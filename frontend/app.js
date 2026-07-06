const api = window.JobApi;
const state = { jobs: [], allJobs: [], selectedId: null, searchTimer: null };
const statusLabels = {
  TO_APPLY:"待投递", APPLIED:"已投递", ASSESSMENT:"测评", WRITTEN_TEST:"笔试",
  FIRST_INTERVIEW:"一面", SECOND_INTERVIEW:"二面", FINAL_INTERVIEW:"终面",
  HR_INTERVIEW:"HR 面", OFFER:"Offer", REJECTED:"已拒绝", GIVEN_UP:"已放弃", CLOSED:"已结束"
};
const priorityLabels = { LOW:"低优先级", MEDIUM:"中优先级", HIGH:"高优先级" };
const $ = (selector) => document.querySelector(selector);

function escapeHtml(value) {
  return String(value ?? "").replace(/[&<>'"]/g, (char) => ({ "&":"&amp;", "<":"&lt;", ">":"&gt;", "'":"&#39;", '"':"&quot;" }[char]));
}

function formatDate(value, fallback = "未设置") {
  if (!value) return fallback;
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? fallback : new Intl.DateTimeFormat("zh-CN", { dateStyle:"medium", timeStyle:"short" }).format(date);
}

function safeExternalUrl(value) {
  if (!value) return null;
  try {
    const url = new URL(value);
    return ["http:", "https:"].includes(url.protocol) ? url.href : null;
  } catch (error) { return null; }
}

function toast(message, type = "success") {
  const element = $("#toast");
  element.textContent = message;
  element.className = `toast show ${type === "error" ? "error" : ""}`;
  clearTimeout(toast.timer);
  toast.timer = setTimeout(() => { element.className = "toast"; }, 2800);
}

function setConnection(online) {
  $(".connection").classList.toggle("online", online);
  $("#connectionText").textContent = online ? "后端服务已连接" : "后端服务未连接";
}

function currentFilters() {
  return {
    keyword: $("#keywordFilter").value.trim(), status: $("#statusFilter").value,
    city: $("#cityFilter").value.trim(), deadlineOrder: $("#deadlineOrder").value
  };
}

function renderStats() {
  const jobs = state.allJobs;
  $("#totalJobs").textContent = jobs.length;
  $("#todoCount").textContent = jobs.filter((job) => job.status === "TO_APPLY").length;
  $("#interviewCount").textContent = jobs.filter((job) => job.status && job.status.includes("INTERVIEW")).length;
  $("#offerCount").textContent = jobs.filter((job) => job.status === "OFFER").length;
}

function renderJobs() {
  $("#resultCount").textContent = `共 ${state.jobs.length} 条`;
  if (!state.jobs.length) {
    $("#jobList").innerHTML = '<div class="empty-state"><strong>没有匹配的岗位</strong><p>调整筛选条件，或新增一条岗位记录。</p></div>';
    return;
  }
  $("#jobList").innerHTML = state.jobs.map((job) => `
    <button class="job-card ${job.id === state.selectedId ? "selected" : ""}" type="button" data-job-id="${job.id}">
      <div class="job-card-head"><div><h3>${escapeHtml(job.jobTitle)}</h3><p class="company">${escapeHtml(job.companyName)}</p></div><span class="pill status">${escapeHtml(statusLabels[job.status] || job.status)}</span></div>
      <div class="job-meta"><span>${escapeHtml(job.city || "城市待确认")}</span><span>截止 ${escapeHtml(formatDate(job.deadline, "未设置"))}</span><span class="pill priority-${escapeHtml(job.priority)}">${escapeHtml(priorityLabels[job.priority] || job.priority)}</span></div>
      ${job.extractedSkills && job.extractedSkills.length ? `<div class="tag-row">${job.extractedSkills.slice(0,5).map((skill) => `<span class="tag">${escapeHtml(skill)}</span>`).join("")}</div>` : ""}
    </button>`).join("");
  document.querySelectorAll("[data-job-id]").forEach((card) => card.addEventListener("click", () => selectJob(Number(card.dataset.jobId))));
}

async function loadJobs(options = {}) {
  $("#jobList").innerHTML = '<div class="loading-state">正在加载岗位…</div>';
  try {
    const [jobs, allJobs] = await Promise.all([api.list(currentFilters()), api.list()]);
    state.jobs = jobs;
    state.allJobs = allJobs;
    setConnection(true);
    renderStats();
    renderJobs();
    if (state.selectedId && !allJobs.some((job) => job.id === state.selectedId)) clearDetail();
    else if (state.selectedId && options.refreshDetail) await selectJob(state.selectedId);
  } catch (error) {
    setConnection(false);
    $("#jobList").innerHTML = `<div class="error-state"><strong>岗位加载失败</strong><p>${escapeHtml(error.message)}</p><button class="secondary" type="button" id="retryBtn">重试</button></div>`;
    $("#retryBtn").addEventListener("click", () => loadJobs());
  }
}

function clearDetail() {
  state.selectedId = null;
  $("#detailPanel").innerHTML = '<div class="empty-state"><strong>选择一个岗位</strong><p>查看岗位详情、修改状态和流转时间线。</p></div>';
  renderJobs();
}

function renderDetail(job, events) {
  const externalUrl = safeExternalUrl(job.jobUrl);
  const statusOptions = Object.entries(statusLabels).map(([value,label]) => `<option value="${value}" ${value === job.status ? "selected" : ""}>${label}</option>`).join("");
  const timeline = events.slice().reverse().map((event) => `<li><strong>${escapeHtml(statusLabels[event.toStatus] || event.toStatus)}</strong>${event.fromStatus ? ` <span class="muted">从 ${escapeHtml(statusLabels[event.fromStatus] || event.fromStatus)}</span>` : ""}${event.note ? `<p>${escapeHtml(event.note)}</p>` : ""}<time>${escapeHtml(formatDate(event.eventTime))}</time></li>`).join("");
  $("#detailPanel").innerHTML = `
    <div class="detail-title"><p class="eyebrow">JOB DETAIL</p><h2>${escapeHtml(job.jobTitle)}</h2><p class="company">${escapeHtml(job.companyName)}</p><div class="detail-actions"><button id="editJobBtn" class="secondary" type="button">编辑</button><button id="deleteJobBtn" class="danger" type="button">删除</button>${externalUrl ? `<a href="${escapeHtml(externalUrl)}" target="_blank" rel="noreferrer">打开岗位链接</a>` : ""}</div></div>
    <dl class="detail-grid"><div><dt>当前状态</dt><dd>${escapeHtml(statusLabels[job.status] || job.status)}</dd></div><div><dt>优先级</dt><dd>${escapeHtml(priorityLabels[job.priority] || job.priority)}</dd></div><div><dt>城市</dt><dd>${escapeHtml(job.city || "未填写")}</dd></div><div><dt>截止时间</dt><dd>${escapeHtml(formatDate(job.deadline))}</dd></div><div><dt>来源</dt><dd>${escapeHtml(job.sourcePlatform || "未填写")}</dd></div><div><dt>岗位方向</dt><dd>${escapeHtml(job.jobDirection || "未填写")}</dd></div></dl>
    ${job.extractedSkills && job.extractedSkills.length ? `<div class="tag-row">${job.extractedSkills.map((skill) => `<span class="tag">${escapeHtml(skill)}</span>`).join("")}</div>` : ""}
    ${job.notes ? `<h3 class="section-title">备注</h3><div class="copy-block">${escapeHtml(job.notes)}</div>` : ""}
    ${job.jdText ? `<h3 class="section-title">JD 原文</h3><div class="copy-block">${escapeHtml(job.jdText)}</div>` : ""}
    <h3 class="section-title">推进状态</h3><form id="statusForm" class="status-editor"><select id="nextStatus" aria-label="新状态">${statusOptions}</select><input id="statusNote" placeholder="本次状态变更备注（选填）"><button class="primary" type="submit">更新状态</button></form>
    <h3 class="section-title">状态时间线</h3><ol class="timeline">${timeline || '<li class="muted">暂无流转记录</li>'}</ol>`;
  $("#editJobBtn").addEventListener("click", () => openEditDialog(job));
  $("#deleteJobBtn").addEventListener("click", () => deleteJob(job));
  $("#statusForm").addEventListener("submit", (event) => changeStatus(event, job));
}

async function selectJob(id) {
  state.selectedId = id;
  renderJobs();
  $("#detailPanel").innerHTML = '<div class="loading-state">正在加载岗位详情…</div>';
  try {
    const [job, events] = await Promise.all([api.get(id), api.events(id)]);
    if (state.selectedId === id) renderDetail(job, events);
  } catch (error) { $("#detailPanel").innerHTML = `<div class="error-state"><strong>详情加载失败</strong><p>${escapeHtml(error.message)}</p></div>`; }
}

function resetForm() {
  $("#jobForm").reset(); $("#jobId").value = ""; $("#formError").textContent = "";
}

function openCreateDialog() {
  resetForm(); $("#dialogTitle").textContent = "新增岗位"; $("#jobDialog").showModal();
}

function toDatetimeLocal(value) {
  if (!value) return "";
  const date = new Date(value); const offset = date.getTimezoneOffset() * 60000;
  return new Date(date.getTime() - offset).toISOString().slice(0,16);
}

function openEditDialog(job) {
  resetForm(); $("#dialogTitle").textContent = "编辑岗位"; $("#jobId").value = job.id;
  const form = $("#jobForm");
  ["companyName","jobTitle","city","sourcePlatform","companyType","jobDirection","priority","jobUrl","jdText","notes"].forEach((key) => { form.elements[key].value = job[key] || ""; });
  form.elements.deadline.value = toDatetimeLocal(job.deadline);
  form.elements.extractedSkills.value = (job.extractedSkills || []).join(", ");
  $("#jobDialog").showModal();
}

function formPayload() {
  const data = new FormData($("#jobForm"));
  const payload = Object.fromEntries(data.entries());
  payload.extractedSkills = payload.extractedSkills.split(/[,，]/).map((item) => item.trim()).filter(Boolean);
  if (!payload.deadline) payload.deadline = null;
  return payload;
}

async function saveJob(event) {
  event.preventDefault();
  const id = $("#jobId").value; const payload = formPayload(); const saveButton = $("#saveJobBtn");
  saveButton.disabled = true; saveButton.textContent = "保存中…"; $("#formError").textContent = "";
  try {
    if (!id) {
      const duplicate = await api.checkDuplicate(payload);
      if (duplicate.duplicate && !window.confirm(`发现 ${duplicate.candidates.length} 条疑似重复岗位，仍要继续保存吗？`)) return;
    }
    const saved = id ? await api.update(id, payload) : await api.create(payload);
    $("#jobDialog").close(); state.selectedId = saved.id; toast(id ? "岗位已更新" : "岗位已创建");
    await loadJobs({ refreshDetail:true });
  } catch (error) { $("#formError").textContent = error.message; }
  finally { saveButton.disabled = false; saveButton.textContent = "保存岗位"; }
}

async function changeStatus(event, job) {
  event.preventDefault(); const status = $("#nextStatus").value;
  if (status === job.status) { toast("请选择不同于当前状态的新状态", "error"); return; }
  try { await api.updateStatus(job.id, status, $("#statusNote").value.trim()); toast("岗位状态已更新"); await loadJobs({ refreshDetail:true }); }
  catch (error) { toast(error.message, "error"); }
}

async function deleteJob(job) {
  if (!window.confirm(`确认删除“${job.companyName} · ${job.jobTitle}”吗？此操作不可撤销。`)) return;
  try { await api.remove(job.id); clearDetail(); toast("岗位已删除"); await loadJobs(); }
  catch (error) { toast(error.message, "error"); }
}

$("#addJobBtn").addEventListener("click", openCreateDialog);
$("#closeDialogBtn").addEventListener("click", () => $("#jobDialog").close());
$("#cancelDialogBtn").addEventListener("click", () => $("#jobDialog").close());
$("#jobForm").addEventListener("submit", saveJob);
$("#refreshBtn").addEventListener("click", () => loadJobs({ refreshDetail:true }));
["#statusFilter","#deadlineOrder"].forEach((selector) => $(selector).addEventListener("change", () => loadJobs()));
["#keywordFilter","#cityFilter"].forEach((selector) => $(selector).addEventListener("input", () => { clearTimeout(state.searchTimer); state.searchTimer = setTimeout(() => loadJobs(), 320); }));
loadJobs();
