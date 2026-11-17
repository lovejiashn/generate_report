<script setup>
import { ref, reactive, onMounted } from 'vue'
import * as echarts from 'echarts/core'
import { BarChart, LineChart } from 'echarts/charts'
import { GridComponent, LegendComponent, TitleComponent, TooltipComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import { renderAsync } from 'docx-preview'

echarts.use([
  BarChart, LineChart,
  GridComponent, LegendComponent, TitleComponent, TooltipComponent,
  CanvasRenderer
])

const API_BASE = import.meta.env.VITE_API_BASE || ''

const previewEl = ref(null)
const previewLoading = ref(false)
const previewed = ref(false)
const fileSize = ref(0)
const currentBlob = ref(null)
const message = ref(null)
const activeTab = ref('form')
const jsonText = ref('')
const expanded = ref({})
const placeholders = ref([])

const draft = reactive({
  title: '',
  typeColor: '',
  typeText: '',
  pictureWidth: 200,
  pictureHeight: 160,
  pictureText: '',
  pictureUrl: '',
  tableHeaders: [],
  tableRows: [],
  listStyle: 'lowerRoman',
  listItems: [],
  charts: []
})

function formatSize(bytes) {
  return bytes < 1024 ? bytes + ' B' : (bytes / 1024).toFixed(1) + ' KB'
}

function notify(type, text) {
  message.value = { type, text }
  if (type === 'success') setTimeout(() => { message.value = null }, 6000)
}

function splitList(text) {
  return (text || '').split(/[\n,]/).map(s => s.trim()).filter(Boolean)
}

function configToDraft(cfg) {
  draft.title = cfg.title || ''
  draft.typeColor = cfg.typeContent?.color || ''
  draft.typeText = cfg.typeContent?.text || ''
  draft.pictureWidth = cfg.picture?.width ?? 200
  draft.pictureHeight = cfg.picture?.height ?? 160
  draft.pictureText = cfg.picture?.text || ''
  draft.pictureUrl = cfg.picture?.url || ''
  draft.tableHeaders = [...(cfg.showTable?.headers || [])]
  draft.tableRows = (cfg.showTable?.rows || []).map(r => [...r])
  draft.listStyle = cfg.numList?.style || 'lowerRoman'
  draft.listItems = [...(cfg.numList?.items || [])]
  draft.charts = (cfg.charts || []).map(c => ({
    labelName: c.labelName,
    title: c.title || '',
    type: c.type || 'multi',
    categoriesText: (c.categories || []).join(', '),
    series: (c.series || []).map(s => ({
      name: s.name || '',
      dataText: (s.data || []).join(', '),
      comboType: s.comboType || ''
    }))
  }))
}

function draftToConfig() {
  return {
    title: draft.title,
    typeContent: { color: draft.typeColor, text: draft.typeText },
    picture: {
      width: Number(draft.pictureWidth),
      height: Number(draft.pictureHeight),
      text: draft.pictureText,
      url: draft.pictureUrl || null
    },
    showTable: {
      headers: [...draft.tableHeaders],
      rows: draft.tableRows.map(r => [...r])
    },
    numList: { style: draft.listStyle, items: [...draft.listItems] },
    charts: draft.charts.map(c => ({
      labelName: c.labelName,
      title: c.title,
      type: c.type,
      categories: splitList(c.categoriesText),
      series: c.series.map(s => ({
        name: s.name,
        data: splitList(s.dataText).map(Number).filter(n => !Number.isNaN(n)),
        comboType: s.comboType || null
      }))
    }))
  }
}

function syncJson() {
  jsonText.value = JSON.stringify(draftToConfig(), null, 2)
}

function buildChartImages() {
  const images = {}
  const host = document.createElement('div')
  host.style.cssText = 'position:absolute;left:-99999px;top:0;width:560px;height:300px;'
  document.body.appendChild(host)
  try {
    draft.charts.forEach(chart => {
      if (!chart.labelName) return
      const instance = echarts.init(host)
      instance.setOption(chartOption(chart), { notMerge: true, lazyUpdate: false })
      images[chart.labelName] = instance.getDataURL({
        type: 'png',
        pixelRatio: 2,
        backgroundColor: '#ffffff',
        excludeComponents: ['tooltip']
      })
      instance.dispose()
    })
  } finally {
    document.body.removeChild(host)
  }
  return images
}

async function renderDocx(cfg) {
  previewLoading.value = true
  message.value = null
  try {
    const payload = { ...cfg, chartImages: buildChartImages() }
    const response = await fetch(`${API_BASE}/report/poitl/preview.do`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    })
    if (!response.ok) throw new Error(`请求失败：HTTP ${response.status}`)
    const blob = await response.blob()
    if (blob.size === 0) throw new Error('返回文件为空')

    previewEl.value.innerHTML = ''
    await renderAsync(blob, previewEl.value, undefined, {
      className: 'docx-body',
      inWrapper: false,
      ignoreWidth: false,
      ignoreHeight: false,
      breakPages: true
    })
    currentBlob.value = blob
    fileSize.value = blob.size
    previewed.value = true
    notify('success', `已按配置重新渲染 Word，文件大小 ${formatSize(blob.size)}`)
  } catch (error) {
    notify('error', `渲染失败：${error.message}`)
  } finally {
    previewLoading.value = false
  }
}

async function applyForm() {
  syncJson()
  await renderDocx(draftToConfig())
}

async function applyJson() {
  try {
    const cfg = JSON.parse(jsonText.value)
    configToDraft(cfg)
    await renderDocx(cfg)
  } catch (error) {
    notify('error', `JSON 解析失败：${error.message}`)
  }
}

async function resetDefault() {
  try {
    const response = await fetch(`${API_BASE}/report/poitl/config.do`)
    const cfg = await response.json()
    configToDraft(cfg)
    syncJson()
    await renderDocx(cfg)
  } catch (error) {
    notify('error', `加载默认配置失败：${error.message}`)
  }
}

async function downloadReport() {
  try {
    let blob = currentBlob.value
    if (!blob) {
      const response = await fetch(`${API_BASE}/report/poitl/download.do`)
      blob = await response.blob()
    }
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = '月度通报函生成报告.docx'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
    notify('success', 'Word 报表已下载')
  } catch (error) {
    notify('error', `下载失败：${error.message}`)
  }
}

function addTableRow() {
  draft.tableRows.push(draft.tableHeaders.map(() => ''))
}
function removeTableRow(index) {
  draft.tableRows.splice(index, 1)
}
function addTableCol() {
  draft.tableHeaders.push(`列${draft.tableHeaders.length + 1}`)
  draft.tableRows.forEach(r => r.push(''))
}
function removeTableCol(index) {
  draft.tableHeaders.splice(index, 1)
  draft.tableRows.forEach(r => r.splice(index, 1))
}
function addListItem() { draft.listItems.push('') }
function removeListItem(index) { draft.listItems.splice(index, 1) }
function addSeries(chart) { chart.series.push({ name: '', dataText: '', comboType: '' }) }
function removeSeries(chart, index) { chart.series.splice(index, 1) }

function chartOption(chart) {
  const categories = splitList(chart.categoriesText)
  const isSingle = chart.type === 'single'
  const series = chart.series.map(s => {
    const combo = (s.comboType || '').toUpperCase()
    const type = combo === 'BAR' ? 'bar' : 'line'
    const item = {
      name: s.name || '系列',
      type,
      data: splitList(s.dataText).map(Number).filter(n => !Number.isNaN(n))
    }
    if (combo === 'AREA' || (isSingle && !combo)) {
      item.areaStyle = {}
    }
    if (type === 'bar') {
      item.itemStyle = { borderRadius: [3, 3, 0, 0] }
    }
    return item
  })
  return {
    animation: false,
    title: { text: chart.title || '未命名图表', left: 'center', textStyle: { fontSize: 12, fontWeight: 600 } },
    tooltip: { trigger: 'axis' },
    legend: { bottom: 0, left: 'center', itemWidth: 12, itemHeight: 8, textStyle: { fontSize: 10 } },
    grid: { left: 52, right: 16, top: 34, bottom: chart.series.length > 1 ? 50 : 34 },
    xAxis: {
      type: 'category',
      data: categories,
      axisLabel: { fontSize: 9, interval: categories.length > 12 ? Math.ceil(categories.length / 10) : 0 }
    },
    yAxis: { type: 'value', axisLabel: { fontSize: 9 } },
    series
  }
}

async function downloadTemplate() {
  try {
    const response = await fetch(`${API_BASE}/report/poitl/template.do`)
    if (!response.ok) throw new Error(`请求失败：HTTP ${response.status}`)
    const blob = await response.blob()
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = '报表默认模板.docx'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
    notify('success', `默认模板已下载，含 {{}} 占位符，大小 ${formatSize(blob.size)}`)
  } catch (error) {
    notify('error', `下载模板失败：${error.message}`)
  }
}

onMounted(async () => {
  const response = await fetch(`${API_BASE}/report/poitl/config.do`)
  const cfg = await response.json()
  configToDraft(cfg)
  syncJson()
  const phResponse = await fetch(`${API_BASE}/report/poitl/placeholders.do`)
  placeholders.value = await phResponse.json()
  await renderDocx(cfg)
})
</script>

<template>
  <div class="page">
    <header class="topbar">
      <div class="brand">
        <div class="brand-mark">W</div>
        <div class="brand-text">
          <strong>Word 报表生成中心</strong>
          <span>基于 poi-tl 模板引擎</span>
        </div>
      </div>
      <div class="status"><i></i> 服务运行中 · :8090</div>
    </header>

    <main class="container">
      <section class="hero">
        <div>
          <p class="eyebrow">REPORT CONFIGURATOR</p>
          <h1>配置内容，实时生成 Word 报表</h1>
          <p class="hero-desc">所有模板内容均可配置，也可直接编辑 JSON 重新渲染。</p>
        </div>
      </section>

      <div class="grid">
        <section class="card preview">
          <div class="card-head">
            <div>
              <h2>文件预览</h2>
              <p>实时渲染按当前配置生成的 Word 文档</p>
            </div>
            <span v-if="previewed" class="ready-tag">已加载 · {{ formatSize(fileSize) }}</span>
          </div>
          <div class="viewer">
            <div v-if="previewLoading && !previewed" class="viewer-state">
              <span class="spinner dark"></span>
              <p>正在生成并加载 Word 文件…</p>
            </div>
            <div ref="previewEl" class="docx-host" v-show="previewed"></div>
          </div>
        </section>

        <aside class="side">
          <section class="card config-card">
            <div class="tabs">
              <button :class="{ active: activeTab === 'form' }" @click="activeTab = 'form'">表单配置</button>
              <button :class="{ active: activeTab === 'json' }" @click="syncJson(); activeTab = 'json'">JSON 配置</button>
              <button :class="{ active: activeTab === 'template' }" @click="activeTab = 'template'">默认模板</button>
            </div>

            <div v-if="activeTab === 'form'" class="form-body">
              <div class="group">
                <div class="group-title">标题文本</div>
                <label class="field"><span>标题内容</span><input v-model="draft.title" /></label>
              </div>

              <div class="group">
                <div class="group-title">带样式文本</div>
                <label class="field"><span>颜色（16进制）</span><input v-model="draft.typeColor" placeholder="cc0000" /></label>
                <label class="field"><span>文本内容</span><input v-model="draft.typeText" /></label>
              </div>

              <div class="group">
                <div class="group-title">图片</div>
                <div class="row2">
                  <label class="field"><span>宽度</span><input v-model.number="draft.pictureWidth" type="number" /></label>
                  <label class="field"><span>高度</span><input v-model.number="draft.pictureHeight" type="number" /></label>
                </div>
                <label class="field"><span>图片文字</span><input v-model="draft.pictureText" /></label>
                <label class="field"><span>图片 URL（可选，填写则使用网络图片）</span><input v-model="draft.pictureUrl" placeholder="留空则生成示例图" /></label>
              </div>

              <div class="group">
                <div class="group-title">表格</div>
                <div class="table-edit">
                  <div class="edit-row">
                    <input v-for="(h, i) in draft.tableHeaders" :key="'h' + i" v-model="draft.tableHeaders[i]" class="cell header-cell" />
                    <button class="mini" @click="addTableCol">+ 列</button>
                  </div>
                  <div v-for="(row, ri) in draft.tableRows" :key="'r' + ri" class="edit-row">
                    <input v-for="(cell, ci) in row" :key="'c' + ci" v-model="row[ci]" class="cell" />
                    <button class="mini danger" @click="removeTableRow(ri)">×</button>
                  </div>
                  <div class="edit-row">
                    <button class="mini" @click="addTableRow">+ 行</button>
                    <button class="mini danger" @click="removeTableCol" v-if="draft.tableHeaders.length">- 列</button>
                  </div>
                </div>
              </div>

              <div class="group">
                <div class="group-title">列表</div>
                <label class="field">
                  <span>编号样式</span>
                  <select v-model="draft.listStyle">
                    <option value="lowerRoman">小写罗马 (i, ii)</option>
                    <option value="upperRoman">大写罗马 (I, II)</option>
                    <option value="decimal">数字 (1, 2)</option>
                    <option value="lowerLetter">小写字母 (a, b)</option>
                    <option value="upperLetter">大写字母 (A, B)</option>
                    <option value="bullet">项目符号</option>
                  </select>
                </label>
                <div v-for="(item, i) in draft.listItems" :key="'li' + i" class="edit-row">
                  <input v-model="draft.listItems[i]" class="cell" />
                  <button class="mini danger" @click="removeListItem(i)">×</button>
                </div>
                <button class="mini" @click="addListItem">+ 列表项</button>
              </div>

              <div class="group">
                <div class="group-title">图表（5 组）</div>
                <div v-for="(chart, ci) in draft.charts" :key="chart.labelName" class="chart-editor">
                  <div class="chart-editor-head" @click="expanded[ci] = !expanded[ci]">
                    <strong>{{ chart.title || '未命名图表' }}</strong>
                    <span class="chart-label">{{ chart.labelName }}</span>
                    <span class="chevron">{{ expanded[ci] ? '−' : '+' }}</span>
                  </div>
                  <div v-if="expanded[ci]" class="chart-editor-body">
                    <label class="field"><span>图表标题</span><input v-model="chart.title" /></label>
                    <label class="field"><span>占位符名称</span><input v-model="chart.labelName" /></label>
                    <label class="field">
                      <span>图表类型</span>
                      <select v-model="chart.type">
                        <option value="multi">多系列</option>
                        <option value="single">单系列</option>
                      </select>
                    </label>
                    <label class="field"><span>分类（逗号分隔）</span><textarea v-model="chart.categoriesText" rows="2"></textarea></label>
                    <div class="series-head">数据系列</div>
                    <div v-for="(s, si) in chart.series" :key="si" class="series-item">
                      <input v-model="s.name" class="cell" placeholder="系列名称" />
                      <input v-model="s.dataText" class="cell" placeholder="数据，逗号分隔" />
                      <select v-model="s.comboType" class="combo">
                        <option value="">默认</option>
                        <option value="BAR">柱形</option>
                        <option value="LINE">折线</option>
                        <option value="AREA">面积</option>
                      </select>
                      <button class="mini danger" @click="removeSeries(chart, si)">×</button>
                    </div>
                    <button class="mini" @click="addSeries(chart)">+ 系列</button>
                  </div>
                </div>
              </div>

              <div class="actions">
                <button class="primary-btn" :disabled="previewLoading" @click="applyForm">
                  {{ previewLoading ? '渲染中…' : '应用配置并重新渲染' }}
                </button>
                <button class="secondary-btn" @click="resetDefault">恢复默认配置</button>
                <button class="secondary-btn" @click="downloadReport">下载当前 Word</button>
              </div>
            </div>

            <div v-else-if="activeTab === 'json'" class="json-body">
              <p class="json-hint">直接编辑 JSON，点击按钮后重新生成 Word。字段与表单配置一致。</p>
              <textarea v-model="jsonText" class="json-editor" spellcheck="false"></textarea>
              <div class="actions">
                <button class="primary-btn" :disabled="previewLoading" @click="applyJson">
                  {{ previewLoading ? '渲染中…' : '应用 JSON 并重新渲染' }}
                </button>
                <button class="secondary-btn" @click="syncJson">从表单同步 JSON</button>
              </div>
            </div>

            <div v-else class="template-body">
              <div class="template-intro">
                <h3>默认 Word 模板</h3>
                <p>模板中使用 <code>&#123;&#123;...&#125;&#125;</code> 占位符标记内容位置，后端渲染时按配置替换。下载后可查看所有占位符及其在文档中的位置。</p>
                <button class="primary-btn" @click="downloadTemplate">下载默认模板 (.docx)</button>
              </div>
              <div class="placeholder-head">占位符与配置项对应关系</div>
              <div class="placeholder-list">
                <div v-for="item in placeholders" :key="item.tag" class="placeholder-item">
                  <code class="placeholder-tag">{{ item.tag }}</code>
                  <div class="placeholder-info">
                    <strong>{{ item.name }} <span class="placeholder-type">{{ item.type }}</span></strong>
                    <span class="placeholder-path">{{ item.configPath }}</span>
                  </div>
                </div>
              </div>
            </div>

            <div v-if="message" class="notice" :class="message.type">
              <span class="notice-icon">{{ message.type === 'success' ? '✓' : '!' }}</span>
              <span>{{ message.text }}</span>
            </div>
          </section>
        </aside>
      </div>
    </main>

    <footer>Vue 3 + Vite 前端 · Spring Boot + poi-tl 后端</footer>
  </div>
</template>
