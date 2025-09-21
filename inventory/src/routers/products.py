from __future__ import annotations

from typing import List, Optional

from fastapi import APIRouter, HTTPException, Query

from ..models import IdList, ProductCreateRequest, ProductDTO
from .. import store

router = APIRouter(prefix="/api/products", tags=["products"])


@router.get("", response_model=List[ProductDTO])
def get_products(
    page: int = Query(0, ge=0),
    size: int = Query(20, ge=1),
    sortBy: str = Query("name"),
    sortDir: str = Query("asc"),
):
    return store.list_products(page=page, size=size, sort_by=sortBy, sort_dir=sortDir)


@router.get("/search", response_model=List[ProductDTO])
def search_products(keyword: str, page: int = Query(0, ge=0), size: int = Query(20, ge=1)):
    return store.search_products(keyword, page=page, size=size)


@router.get("/{id}", response_model=ProductDTO)
def get_product(id: str):
    p = store.find_product_by_id(id)
    if not p:
        raise HTTPException(status_code=404, detail="Product not found")
    return p


@router.get("/sku/{sku}", response_model=ProductDTO)
def get_product_by_sku(sku: str):
    p = store.find_product_by_sku(sku)
    if not p:
        raise HTTPException(status_code=404, detail="Product not found")
    return p


@router.get("/category/{categoryId}", response_model=List[ProductDTO])
def get_products_by_category(categoryId: str, page: int = Query(0, ge=0), size: int = Query(20, ge=1)):
    return store.list_products_by_category(categoryId, page=page, size=size)


@router.post("/batch", response_model=List[ProductDTO])
def get_products_by_ids(ids: IdList):
    return store.find_products_by_ids(ids.root)


@router.post("", response_model=ProductDTO, status_code=201)
def create_product(req: ProductCreateRequest):
    cat = store.get_category(req.categoryId)
    if not cat:
        raise HTTPException(status_code=400, detail="Invalid categoryId")
    return store.create_product(req, category=cat)
