# Brand Screens Flow - How It Works

## 📱 Screen Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    BRANDS FLOW DIAGRAM                        │
└─────────────────────────────────────────────────────────────┘

Bottom Nav "Brands" Tab
         │
         ▼
┌─────────────────────┐
│  BrandsFragment     │  ← Entry Point
│  (BrandsScreen)     │
└──────────┬──────────┘
           │
           ├─────────────────┬──────────────────┐
           │                 │                  │
           ▼                 ▼                  ▼
    ┌─────────────┐  ┌──────────────┐  ┌──────────────┐
    │ Grid View   │  │ "View All >" │  │ Brand Click  │
    │ (3 columns) │  │   Click      │  │              │
    └─────────────┘  └──────┬───────┘  └──────┬───────┘
                            │                  │
                            ▼                  │
                    ┌──────────────┐          │
                    │ AllBrands    │          │
                    │ Screen       │          │
                    │ (List View)  │          │
                    └──────┬───────┘          │
                           │                  │
                           │ Brand Click      │
                           │                  │
                           └────────┬─────────┘
                                    │
                                    ▼
                    ┌──────────────────────────┐
                    │ BrandProductsFragment    │
                    │ (BrandProductsScreen)    │
                    │ Shows Products Grid      │
                    └──────────┬───────────────┘
                               │
                               │ Product Click
                               │
                               ▼
                    ┌──────────────────────────┐
                    │ ProductDetailsFragment   │
                    └──────────────────────────┘
```

## 🔄 Data Flow

### 1. **Initial Load (BrandsFragment)**

```
BrandsFragment.onCreateView()
    │
    ├─> Creates BrandViewModel (via Hilt)
    │
    └─> BrandsScreen Composable
        │
        ├─> BrandViewModel.init { fetchBrands() }
        │   │
        │   └─> API Call: GET /rest/V1/giftexpress/brands
        │       │
        │       └─> Updates: brandsState (UiState<List<BrandResponse>>)
        │
        └─> BrandsGridScreen (default view)
            │
            ├─> Displays brands in 3-column grid
            ├─> Shows "POPULAR BRANDS" header
            └─> Each brand card is clickable
```

### 2. **View All Brands Flow**

```
User clicks "View All >"
    │
    ▼
BrandsScreen.onViewAllClick()
    │
    └─> Sets showAllBrands = true
        │
        └─> Shows AllBrandsScreen composable
            │
            ├─> Uses same brandsState from ViewModel
            ├─> Groups brands alphabetically
            ├─> Shows search bar
            ├─> Shows alphabetical index (A-Z)
            └─> Brand items are clickable
```

### 3. **Brand Click Flow**

```
User clicks a brand (from grid or list)
    │
    ▼
onBrandClick(brandId, brandName)
    │
    └─> BrandsFragment creates Bundle
        │   ├─> brandId: Int
        │   └─> brandName: String
        │
        └─> Navigation: navigate(R.id.brandProductsFragment, bundle)
            │
            └─> BrandProductsFragment.onCreateView()
                │
                ├─> Receives args via navArgs()
                ├─> Creates BrandViewModel (shared instance)
                │
                └─> BrandProductsScreen
                    │
                    ├─> LaunchedEffect(brandId) triggers:
                    │   │
                    │   ├─> viewModel.resetPagination()
                    │   └─> viewModel.fetchBrandProducts(brandId, reset=true)
                    │       │
                    │       └─> API Call: GET /rest/V1/giftexpress/brand/{brandId}/products?pageSize=20&currentPage=1
                    │           │
                    │           └─> Updates: brandProductsState (UiState<List<SliderProduct>>)
                    │
                    └─> Displays products in 3-column grid
                        │
                        └─> Pagination: When scrolling to last item
                            └─> viewModel.loadNextPage()
                                └─> Fetches next page and appends to list
```

### 4. **Product Click Flow**

```
User clicks a product in BrandProductsScreen
    │
    ▼
onProductClick(sku: String)
    │
    └─> BrandProductsFragment creates Bundle
        │   └─> sku: String
        │
        └─> Navigation: navigate(R.id.productDetailsFragment, bundle)
            │
            └─> ProductDetailsFragment (existing screen)
```

## 🗂️ File Structure

```
app/src/main/java/com/giftexpress/app/ui/brands/
│
├── BrandsFragment.kt              # Main entry fragment
├── BrandsScreen.kt                # Main composable (handles grid/list toggle)
├── AllBrandsScreen.kt             # Alphabetical list view composable
├── BrandProductsScreen.kt         # Products grid for selected brand
├── BrandProductsFragment.kt       # Fragment for brand products
├── BrandViewModel.kt              # ViewModel (manages state & API calls)
│
└── data/
    ├── model/
    │   └── BrandResponse.kt        # Data classes
    └── repository/
        └── BrandRepository.kt     # API calls
```

## 🔌 API Endpoints Used

### 1. **Get Brand List**
```
GET: /rest/V1/giftexpress/brands
Response: List<BrandResponse>
  - id: Int
  - name: String
  - image: String?
  - url: String?
  - categoryId: Int?
```

### 2. **Get Brand Products**
```
GET: /rest/V1/giftexpress/brand/{brandId}/products?pageSize=20&currentPage=1
Response: BrandProductsResponse
  - items: List<SliderProduct>
  - totalCount: Int
  - pageSize: Int
  - currentPage: Int
  - totalPages: Int
```

## 📊 State Management

### BrandViewModel States:

1. **brandsState**: `StateFlow<UiState<List<BrandResponse>>>`
   - Loading: Shows CircularProgressIndicator
   - Success: Shows brands grid/list
   - Error: Shows error message

2. **brandProductsState**: `StateFlow<UiState<List<SliderProduct>>>`
   - Loading: Shows CircularProgressIndicator
   - Success: Shows products grid
   - Error: Shows error message

### Pagination Logic:

```kotlin
// Accumulates products in allProducts list
allProducts.addAll(response.items ?: emptyList())
_brandProductsState.value = UiState.Success(allProducts.toList())
currentPage++

// Loads next page when scrolling to last item
LaunchedEffect(products.size) {
    if (products.indexOf(product) == products.size - 1) {
        viewModel.loadNextPage()
    }
}
```

## 🎨 UI Components

### BrandsGridScreen
- **Header**: HomeHeader (with search)
- **Content**: 3-column grid of brand cards
- **Footer**: "POPULAR BRANDS" with "View All >" link

### AllBrandsScreen
- **Top Bar**: Back button + "Brands" title
- **Search Bar**: Filters brands in real-time
- **Content**: Alphabetical list grouped by letter
- **Side Index**: A-Z navigation (highlighted letter in red)

### BrandProductsScreen
- **Top Bar**: Brand name + back/search/cart/wishlist icons
- **Content**: 3-column product grid
- **Bottom Bar**: Filter & Sort buttons
- **Pagination**: Auto-loads more products on scroll

## 🔑 Key Features

1. **State Sharing**: BrandViewModel is shared between screens (via Hilt)
2. **Conditional Rendering**: BrandsScreen toggles between grid and list views
3. **Search**: Real-time filtering in AllBrandsScreen
4. **Pagination**: Infinite scroll for brand products
5. **Navigation**: Uses Navigation Component with safe args
6. **Error Handling**: NetworkResult wrapper for API calls

## 🚀 User Journey Example

1. User taps "Brands" in bottom navigation
2. Sees grid of popular brands (3 columns)
3. Clicks "View All >" → Sees alphabetical list with search
4. Searches for "B" → Sees filtered brands starting with B
5. Clicks "Burberry" → Sees Burberry products in grid
6. Scrolls down → More products load automatically
7. Clicks a product → Sees product details
