from fastapi.testclient import TestClient

from src.main import app


client = TestClient(app)


def test_health_root():
    r = client.get("/")
    assert r.status_code == 200
    assert r.json()["message"].startswith("Hello")


def test_category_and_product_flow():
    # create category
    c = client.post(
        "/api/categories",
        json={
            "name": "Skis",
            "description": "Ski category",
        },
    )
    assert c.status_code == 201, c.text
    cat_id = c.json()["id"]

    # create product under category
    p = client.post(
        "/api/products",
        json={
            "sku": "SKI-ATOMIC-001",
            "name": "Atomic Bent 100 Ski",
            "description": "Versatile all-mountain ski",
            "brand": "Atomic",
            "attributes": {"length": 180},
            "tags": ["ski", "men"],
            "categoryId": cat_id,
            "price": {"regularPrice": 65000, "salePrice": 58500, "currencyCode": "JPY"},
            "inventory": {"quantity": 15, "locationCode": "TOKYO-A"},
        },
    )
    assert p.status_code == 201, p.text
    prod = p.json()
    pid = prod["id"]

    # get by id
    g = client.get(f"/api/products/{pid}")
    assert g.status_code == 200
    assert g.json()["sku"] == "SKI-ATOMIC-001"

    # get inventory status
    inv = client.get(f"/api/inventory/status/{pid}")
    assert inv.status_code == 200
    assert inv.json()["availableQuantity"] == 15

    # reserve stock
    rs = client.post("/api/inventory/reserve", json={"productId": pid, "quantity": 2})
    assert rs.status_code == 200
    status = client.get(f"/api/inventory/status/{pid}").json()
    assert status["availableQuantity"] == 13

    # release stock
    rel = client.post("/api/inventory/release", json={"productId": pid, "quantity": 1})
    assert rel.status_code == 200
    status = client.get(f"/api/inventory/status/{pid}").json()
    assert status["availableQuantity"] == 14

    # stock-out more than available should fail
    so_fail = client.post("/api/inventory/stock-out", json={"productId": pid, "quantity": 999})
    assert so_fail.status_code == 400

    # stock-in
    si = client.post("/api/inventory/stock-in", json={"productId": pid, "quantity": 10})
    assert si.status_code == 200
    status = client.get(f"/api/inventory/status/{pid}").json()
    assert status["availableQuantity"] == 24
