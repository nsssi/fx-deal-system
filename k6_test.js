import http from 'k6/http';
import { check, fail, sleep } from 'k6';

export let options = {
    vus: 10,
    duration: '15s',
};

const BASE = 'http://localhost:8080/api/deals';

export default function () {

    // ---------------------------------------------------------
    // 1 HEALTH CHECK
    // ---------------------------------------------------------
    let health = http.get(BASE + "/health");

    check(health, {
        "Health Check → 200": (r) => r.status === 200
    }) || fail("Health check failed");

    // ---------------------------------------------------------
    // 2 IMPORT SINGLE DEAL (VALID)
    // ---------------------------------------------------------
    let id = `K6_SINGLE_${__VU}_${Date.now()}`;

    let singlePayload = JSON.stringify({
        dealUniqueId: id,
        fromCurrencyIsoCode: "USD",
        toCurrencyIsoCode: "EUR",
        dealTimestamp: "2024-01-01T10:00:00",
        dealAmount: 999
    });

    let resSingle = http.post(BASE, singlePayload, {
        headers: { "Content-Type": "application/json" }
    });

    check(resSingle, {
        "POST single deal → 201": (r) => r.status === 201
    }) || fail("Failed POST /deals");

    // ---------------------------------------------------------
    // 3 IMPORT BULK DEALS (VALID)
    // ---------------------------------------------------------
    let bulkPayload = JSON.stringify([
        {
            dealUniqueId: `K6_BULK_${__VU}_${Date.now()}_1`,
            fromCurrencyIsoCode: "USD",
            toCurrencyIsoCode: "EUR",
            dealTimestamp: "2024-01-01T10:00:00",
            dealAmount: 100
        },
        {
            dealUniqueId: `K6_BULK_${__VU}_${Date.now()}_2`,
            fromCurrencyIsoCode: "GBP",
            toCurrencyIsoCode: "USD",
            dealTimestamp: "2024-01-01T10:00:00",
            dealAmount: 200
        }
    ]);

    let resBulk = http.post(BASE + "/bulk", bulkPayload, {
        headers: { "Content-Type": "application/json" }
    });

    check(resBulk, {
        "POST bulk → 201": (r) => r.status === 201
    }) || fail("Failed POST /deals/bulk");

    // ---------------------------------------------------------
    // 4 GET ALL DEALS
    // ---------------------------------------------------------
    let resGetAll = http.get(BASE);

    check(resGetAll, {
        "GET all deals → 200": (r) => r.status === 200,
        "GET all deals returns array": (r) => r.json().length >= 1
    }) || fail("Failed GET /deals");

    // ---------------------------------------------------------
    // 5 GET DEAL BY ID
    // ---------------------------------------------------------
    let resGetId = http.get(`${BASE}/${id}`);

    check(resGetId, {
        "GET deal by ID → 200": (r) => r.status === 200,
        "GET deal returns correct ID": (r) => r.json().dealUniqueId === id
    }) || fail("Failed GET /deals/{id}");

    sleep(1);
}
