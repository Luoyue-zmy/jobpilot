(function (root, factory) {
  const api = factory(root.fetch && root.fetch.bind(root));
  if (typeof module === "object" && module.exports) module.exports = factory;
  else root.JobApi = api;
})(typeof globalThis !== "undefined" ? globalThis : window, function createJobApi(fetchImpl, baseUrl) {
  const apiBase = baseUrl || "http://localhost:8080/api";

  async function request(path, options) {
    if (!fetchImpl) throw new Error("当前环境不支持网络请求");
    let response;
    try {
      response = await fetchImpl(`${apiBase}${path}`, {
        headers: { "Content-Type": "application/json", ...(options && options.headers) },
        ...options
      });
    } catch (error) {
      throw new Error("无法连接后端服务，请确认服务已在 8080 端口启动");
    }
    let payload;
    try { payload = await response.json(); } catch (error) { throw new Error(`服务返回了无法解析的响应（HTTP ${response.status}）`); }
    if (!response.ok || !payload.success) throw new Error(payload.message || `请求失败（HTTP ${response.status}）`);
    return payload.data;
  }

  function queryString(params) {
    const query = new URLSearchParams();
    Object.entries(params || {}).forEach(([key, value]) => {
      if (value !== undefined && value !== null && String(value).trim() !== "") query.set(key, value);
    });
    const text = query.toString();
    return text ? `?${text}` : "";
  }

  return {
    list: (filters) => request(`/jobs${queryString(filters)}`),
    get: (id) => request(`/jobs/${id}`),
    create: (job) => request("/jobs", { method: "POST", body: JSON.stringify(job) }),
    update: (id, job) => request(`/jobs/${id}`, { method: "PUT", body: JSON.stringify(job) }),
    remove: (id) => request(`/jobs/${id}`, { method: "DELETE" }),
    updateStatus: (id, status, note) => request(`/jobs/${id}/status`, { method: "PUT", body: JSON.stringify({ status, note }) }),
    events: (id) => request(`/jobs/${id}/events`),
    checkDuplicate: (job) => request("/jobs/check-duplicate", { method: "POST", body: JSON.stringify(job) })
  };
});
