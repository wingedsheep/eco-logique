# Product Module

The Product module is responsible for managing the catalog of eco-friendly products. It handles product creation, retrieval, updates, and deletion, while enforcing domain-specific rules related to sustainability.

## Key Features

- **Product Management (CRUD)**: Create, Read, Update, and Delete products.
- **Categorization**: Products are categorized into specific eco-friendly categories (e.g., CLOTHING, HOUSEHOLD, ELECTRONICS, FOOD, PERSONAL_CARE).
- **Sustainability Rating**: Automatically calculates a sustainability rating (A+, A, B, C, D) based on the product category and its carbon footprint.
- **Price Management**: Supports product pricing in multiple currencies (though currently defaulted to EUR/USD in examples).

## Domain Model

### Product

The core entity representing an item for sale.

- **Name**: Unique name of the product.
- **Category**: The type of product.
- **Price**: Monetary value.
- **Weight**: Physical weight in grams.
- **Carbon Footprint**: The CO2 emission associated with the product (kg CO2).
- **Sustainability Rating**: A derived rating indicating how eco-friendly the product is.

### Sustainability Rating Logic

The rating is determined by the `SustainabilityRating.calculate(category, carbonFootprint)` domain service. It compares the carbon footprint against thresholds specific to the category.

## API Usage

### Create Product

`POST /api/v1/products`

```json
{
  "name": "Organic Cotton T-Shirt",
  "description": "Sustainable cotton t-shirt",
  "category": "CLOTHING",
  "priceAmount": 29.99,
  "priceCurrency": "EUR",
  "weightGrams": 150,
  "carbonFootprintKg": 2.1
}
```

### Get Product

`GET /api/v1/products/{id}`

### Update Price

`PUT /api/v1/products/{id}/price`

```json
{
  "priceAmount": 24.99,
  "priceCurrency": "EUR"
}
```

### Delete Product

`DELETE /api/v1/products/{id}`
