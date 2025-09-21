from __future__ import annotations

from datetime import datetime
from decimal import Decimal
from typing import Any, Dict, List, Optional
from uuid import UUID, uuid4

from pydantic import BaseModel, Field, RootModel, field_validator


# ---------- Category Models ----------


class CategoryBase(BaseModel):
    name: str = Field(..., max_length=100)
    description: Optional[str] = Field(None, max_length=500)
    parentId: Optional[str] = None
    sortOrder: Optional[int] = None
    isVisible: Optional[bool] = True
    imageUrl: Optional[str] = None


class CategoryCreateRequest(CategoryBase):
    pass


class CategoryUpdateRequest(CategoryBase):
    pass


class CategoryDTO(BaseModel):
    id: str
    name: str
    description: Optional[str] = None
    parentId: Optional[str] = None
    parent: Optional["CategoryDTO"] = None
    children: List["CategoryDTO"] = Field(default_factory=list)
    level: Optional[int] = None
    path: Optional[str] = None
    active: Optional[bool] = True
    createdAt: Optional[datetime] = None
    updatedAt: Optional[datetime] = None
    productCount: Optional[int] = 0

    def isRoot(self) -> bool:
        return not self.parentId

    def hasChildren(self) -> bool:
        return bool(self.children)

    def isAvailable(self) -> bool:
        return bool(self.active)


# ---------- Product Models ----------


class ProductImageDTO(BaseModel):
    id: UUID = Field(default_factory=uuid4)
    productId: str
    url: str = Field(..., max_length=500)
    thumbnailUrl: Optional[str] = Field(None, max_length=500)
    type: str
    sortOrder: int = Field(0, ge=0)
    altText: Optional[str] = Field(None, max_length=200)
    createdAt: Optional[datetime] = None
    updatedAt: Optional[datetime] = None

    def isMainImage(self) -> bool:
        return self.type.lower() == "main"

    def hasThumbnail(self) -> bool:
        return bool(self.thumbnailUrl)


class PriceInfoDTO(BaseModel):
    regularPrice: Decimal = Field(..., gt=0)
    salePrice: Optional[Decimal] = Field(None, gt=0)
    currentPrice: Optional[Decimal] = None
    currencyCode: str = Field("JPY", min_length=3, max_length=3)
    onSale: Optional[bool] = None
    saleStartDate: Optional[datetime] = None
    saleEndDate: Optional[datetime] = None

    @field_validator("currentPrice", mode="before")
    @classmethod
    def compute_current_price(cls, v, values):
        if v is not None:
            return v
        sale_price = values.get("salePrice")
        regular = values.get("regularPrice")
        on_sale = values.get("onSale")
        if on_sale and sale_price is not None:
            return sale_price
        return regular

    def isValidSalePeriod(self) -> bool:
        if not self.onSale:
            return False
        now = datetime.now()
        after_start = self.saleStartDate is None or now >= self.saleStartDate
        before_end = self.saleEndDate is None or now <= self.saleEndDate
        return after_start and before_end


class InventoryInfoDTO(BaseModel):
    status: Optional[str] = None
    quantity: int = Field(0, ge=0)
    availableQuantity: int = Field(0, ge=0)
    reservedQuantity: int = Field(0, ge=0)
    locationCode: Optional[str] = None

    def getStockStatus(self) -> str:
        qty = self.availableQuantity or 0
        if qty == 0:
            return "Out of Stock"
        if qty <= 5:
            return "Low Stock"
        return "In Stock" if qty > 10 else "Limited Stock"


class ProductDTO(BaseModel):
    id: Optional[str] = None
    sku: str = Field(..., max_length=50)
    name: str = Field(..., max_length=200)
    description: Optional[str] = Field(None, max_length=1000)
    brand: Optional[str] = Field(None, max_length=100)
    attributes: Dict[str, Any] = Field(default_factory=dict)
    tags: List[str] = Field(default_factory=list)
    category: Optional[CategoryDTO] = None
    price: Optional[PriceInfoDTO] = None
    inventory: Optional[InventoryInfoDTO] = None
    images: List[ProductImageDTO] = Field(default_factory=list)
    imageUrl: Optional[str] = None
    active: Optional[bool] = True
    createdAt: Optional[datetime] = None
    updatedAt: Optional[datetime] = None

    def isActive(self) -> bool:
        return bool(self.active)

    def isOnSale(self) -> bool:
        return bool(self.price and self.price.onSale)

    def hasStock(self) -> bool:
        return bool(self.inventory and (self.inventory.availableQuantity or 0) > 0)


class ProductCreatePriceRequest(BaseModel):
    regularPrice: Decimal = Field(..., gt=0)
    salePrice: Optional[Decimal] = Field(None, gt=0)
    saleStartDate: Optional[datetime] = None
    saleEndDate: Optional[datetime] = None
    currencyCode: str = Field("JPY", min_length=3, max_length=3)


class ProductCreateInventoryRequest(BaseModel):
    quantity: int = Field(..., ge=0)
    locationCode: str = Field(..., max_length=20)


class ProductCreateRequest(BaseModel):
    sku: str = Field(..., max_length=50)
    name: str = Field(..., max_length=200)
    description: Optional[str] = Field(None, max_length=1000)
    brand: Optional[str] = Field(None, max_length=100)
    attributes: Dict[str, Any] = Field(default_factory=dict)
    tags: List[str] = Field(default_factory=list)
    categoryId: str
    price: ProductCreatePriceRequest
    inventory: ProductCreateInventoryRequest


# ---------- Inventory Models ----------


class Inventory(BaseModel):
    productId: str
    status: str = "IN_STOCK"
    quantity: int = Field(0, ge=0)
    reservedQuantity: int = Field(0, ge=0)
    locationCode: Optional[str] = None

    @property
    def availableQuantity(self) -> int:
        return max(0, self.quantity - self.reservedQuantity)


class InventoryStatusResponse(BaseModel):
    productId: str
    status: str
    quantity: int
    reservedQuantity: int
    availableQuantity: int
    inStock: bool

    @classmethod
    def of(cls, inv: Inventory) -> "InventoryStatusResponse":
        avail = inv.availableQuantity
        return cls(
            productId=inv.productId,
            status=inv.status,
            quantity=inv.quantity,
            reservedQuantity=inv.reservedQuantity,
            availableQuantity=avail,
            inStock=avail > 0,
        )


class IdList(RootModel[List[str]]):
    root: List[str]


class StockQuantityRequest(BaseModel):
    productId: str
    quantity: int = Field(..., ge=1)
