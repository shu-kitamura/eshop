from __future__ import annotations

from datetime import datetime
from typing import Dict, List, Optional

from .models import (
    CategoryCreateRequest,
    CategoryDTO,
    Inventory,
    InventoryStatusResponse,
    ProductCreateRequest,
    ProductDTO,
    InventoryInfoDTO,
    PriceInfoDTO,
)


# In-memory stores (simple dicts). In real app, replace with DB.
_categories: Dict[str, CategoryDTO] = {}
_products: Dict[str, ProductDTO] = {}
_inventory: Dict[str, Inventory] = {}


def _now() -> datetime:
    return datetime.now()


# ---------- Category operations ----------


def create_category(req: CategoryCreateRequest) -> CategoryDTO:
    cat_id = f"cat_{len(_categories) + 1}"
    dto = CategoryDTO(
        id=cat_id,
        name=req.name,
        description=req.description,
        parentId=req.parentId,
        level=1,
        path=f"/{req.name}" if req.name else None,
        active=True,
        createdAt=_now(),
        updatedAt=_now(),
        productCount=0,
    )
    _categories[cat_id] = dto
    return dto


def get_category(cat_id: str) -> Optional[CategoryDTO]:
    return _categories.get(cat_id)


def list_categories(search: Optional[str] = None, page: int = 0, size: int = 20) -> List[CategoryDTO]:
    items = list(_categories.values())
    if search:
        q = search.lower()
        items = [c for c in items if q in c.name.lower()]
    start = page * size
    end = start + size
    return items[start:end]


# ---------- Product operations ----------


def create_product(req: ProductCreateRequest, category: Optional[CategoryDTO]) -> ProductDTO:
    pid = f"prod_{len(_products) + 1}"
    price = PriceInfoDTO(
        regularPrice=req.price.regularPrice,
        salePrice=req.price.salePrice,
        currencyCode=req.price.currencyCode,
        onSale=req.price.salePrice is not None,
        saleStartDate=req.price.saleStartDate,
        saleEndDate=req.price.saleEndDate,
    )
    inv_info = InventoryInfoDTO(
        status="IN_STOCK" if (req.inventory.quantity or 0) > 0 else "OUT_OF_STOCK",
        quantity=req.inventory.quantity,
        reservedQuantity=0,
        availableQuantity=req.inventory.quantity,
        locationCode=req.inventory.locationCode,
    )
    dto = ProductDTO(
        id=pid,
        sku=req.sku,
        name=req.name,
        description=req.description,
        brand=req.brand,
        attributes=req.attributes,
        tags=req.tags,
        category=category,
        price=price,
        inventory=inv_info,
        images=[],
        imageUrl=None,
        active=True,
        createdAt=_now(),
        updatedAt=_now(),
    )
    _products[pid] = dto

    # also create inventory record
    _inventory[pid] = Inventory(
        productId=pid,
        status=inv_info.status or "IN_STOCK",
        quantity=inv_info.quantity,
        reservedQuantity=inv_info.reservedQuantity,
        locationCode=inv_info.locationCode,
    )
    return dto


def find_product_by_id(pid: str) -> Optional[ProductDTO]:
    return _products.get(pid)


def find_product_by_sku(sku: str) -> Optional[ProductDTO]:
    for p in _products.values():
        if p.sku == sku:
            return p
    return None


def list_products(page: int = 0, size: int = 20, sort_by: str = "name", sort_dir: str = "asc") -> List[ProductDTO]:
    items = list(_products.values())
    reverse = sort_dir.lower() == "desc"
    try:
        items.sort(key=lambda x: getattr(x, sort_by) or "", reverse=reverse)
    except Exception:
        items.sort(key=lambda x: x.name or "", reverse=reverse)
    start = page * size
    end = start + size
    return items[start:end]


def search_products(keyword: str, page: int = 0, size: int = 20) -> List[ProductDTO]:
    q = keyword.lower()
    items = [p for p in _products.values() if q in (p.name or "").lower() or q in (p.description or "").lower()]
    start = page * size
    end = start + size
    return items[start:end]


def list_products_by_category(category_id: str, page: int = 0, size: int = 20) -> List[ProductDTO]:
    items = [p for p in _products.values() if p.category and p.category.id == category_id]
    start = page * size
    end = start + size
    return items[start:end]


def find_products_by_ids(ids: List[str]) -> List[ProductDTO]:
    return [p for pid, p in _products.items() if pid in ids]


# ---------- Inventory operations ----------


def get_inventory(product_id: str) -> Optional[Inventory]:
    return _inventory.get(product_id)


def get_inventory_status(product_id: str) -> Optional[InventoryStatusResponse]:
    inv = _inventory.get(product_id)
    return InventoryStatusResponse.of(inv) if inv else None


def get_inventory_batch(product_ids: List[str]) -> Dict[str, Inventory]:
    return {pid: inv for pid, inv in _inventory.items() if pid in product_ids}


def reserve_stock(product_id: str, quantity: int) -> None:
    inv = _inventory.get(product_id)
    if not inv:
        raise KeyError("Inventory not found")
    if inv.availableQuantity < quantity:
        raise ValueError("Insufficient stock to reserve")
    inv.reservedQuantity += quantity


def release_stock(product_id: str, quantity: int) -> None:
    inv = _inventory.get(product_id)
    if not inv:
        raise KeyError("Inventory not found")
    inv.reservedQuantity = max(0, inv.reservedQuantity - quantity)


def stock_in(product_id: str, quantity: int) -> None:
    inv = _inventory.get(product_id)
    if not inv:
        _inventory[product_id] = Inventory(productId=product_id, quantity=quantity, reservedQuantity=0)
    else:
        inv.quantity += quantity


def stock_out(product_id: str, quantity: int) -> None:
    inv = _inventory.get(product_id)
    if not inv:
        raise KeyError("Inventory not found")
    if inv.availableQuantity < quantity:
        raise ValueError("Insufficient available quantity to stock out")
    inv.quantity = max(0, inv.quantity - quantity)


def low_stock_items(threshold: int = 5) -> List[Inventory]:
    return [inv for inv in _inventory.values() if inv.availableQuantity <= threshold]
