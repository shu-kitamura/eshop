from fastapi import FastAPI

from .routers.categories import router as categories_router
from .routers.products import router as products_router
from .routers.inventory import router as inventory_router

app = FastAPI()


@app.get("/")
async def root():
    return {"message": "Hello from inventory!"}


# Include API routers
app.include_router(categories_router)
app.include_router(products_router)
app.include_router(inventory_router)
