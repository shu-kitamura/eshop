from __future__ import annotations

from typing import Dict, List

from fastapi import APIRouter, HTTPException, Query

from ..models import Inventory, InventoryStatusResponse, StockQuantityRequest, IdList
from .. import store

router = APIRouter(prefix="/api/inventory", tags=["inventory"])


@router.get("/{productId}", response_model=Inventory)
def get_inventory(productId: str):
    inv = store.get_inventory(productId)
    if not inv:
        raise HTTPException(status_code=404, detail="Inventory not found")
    return inv


@router.get("/status/{productId}", response_model=InventoryStatusResponse)
def get_inventory_status(productId: str):
    resp = store.get_inventory_status(productId)
    if not resp:
        raise HTTPException(status_code=404, detail="Inventory not found")
    return resp


@router.post("/batch", response_model=Dict[str, Inventory])
def get_inventory_batch(ids: IdList):
    return store.get_inventory_batch(ids.root)


@router.post("/reserve")
def reserve_stock(req: StockQuantityRequest):
    try:
        store.reserve_stock(req.productId, req.quantity)
        return {"message": "Stock reservation completed"}
    except KeyError:
        raise HTTPException(status_code=404, detail="Inventory not found")
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))


@router.post("/release")
def release_stock(req: StockQuantityRequest):
    try:
        store.release_stock(req.productId, req.quantity)
        return {"message": "Stock reservation released"}
    except KeyError:
        raise HTTPException(status_code=404, detail="Inventory not found")


@router.post("/stock-in")
def stock_in(req: StockQuantityRequest):
    store.stock_in(req.productId, req.quantity)
    return {"message": "Stock in process completed"}


@router.post("/stock-out")
def stock_out(req: StockQuantityRequest):
    try:
        store.stock_out(req.productId, req.quantity)
        return {"message": "Stock out process completed"}
    except KeyError:
        raise HTTPException(status_code=404, detail="Inventory not found")
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))


@router.get("/low-stock", response_model=List[Inventory])
def get_low_stock(threshold: int = Query(5, ge=0)):
    return store.low_stock_items(threshold)
