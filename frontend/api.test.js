const assert = require("assert");
const createJobApi = require("./api.js");

async function run() {
  const calls = [];
  const fetchMock = async (url, options = {}) => {
    calls.push({ url, options });
    return { ok: true, status: 200, json: async () => ({ success: true, data: [{ id: 1 }] }) };
  };
  const api = createJobApi(fetchMock, "http://test/api");
  const jobs = await api.list({ keyword: "Java 后端", status: "APPLIED", city: "" });
  assert.deepStrictEqual(jobs, [{ id: 1 }]);
  assert.strictEqual(calls[0].url, "http://test/api/jobs?keyword=Java+%E5%90%8E%E7%AB%AF&status=APPLIED");

  await api.updateStatus(7, "OFFER", "通过终面");
  assert.strictEqual(calls[1].options.method, "PUT");
  assert.deepStrictEqual(JSON.parse(calls[1].options.body), { status: "OFFER", note: "通过终面" });

  const failingApi = createJobApi(async () => ({ ok: false, status: 400, json: async () => ({ success: false, message: "companyName is required" }) }), "http://test/api");
  await assert.rejects(() => failingApi.create({}), /companyName is required/);
  console.log("frontend api tests passed");
}

run().catch((error) => { console.error(error); process.exitCode = 1; });
