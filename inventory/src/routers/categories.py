from __future__ import annotations

from typing import List, Optional

from fastapi import APIRouter, HTTPException, Query

from ..models import CategoryCreateRequest, CategoryDTO
from .. import store

router = APIRouter(prefix="/api/categories", tags=["categories"])


@router.get("", response_model=List[CategoryDTO])
def get_categories(page: int = Query(0, ge=0), size: int = Query(20, ge=1), search: Optional[str] = None):
    return store.list_categories(search=search, page=page, size=size)


@router.get("/{id}", response_model=CategoryDTO)
def get_category(id: str):
    c = store.get_category(id)
    if not c:
        raise HTTPException(status_code=404, detail="Category not found")
    return c


@router.post("", response_model=CategoryDTO, status_code=201)
def create_category(req: CategoryCreateRequest):
    return store.create_category(req)
