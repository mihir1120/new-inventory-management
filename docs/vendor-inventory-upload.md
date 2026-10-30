# Vendor inventory upload

In the admin app, open **Plants**, **Pots**, or **Supplies**, tap **Add item**, then choose **Excel upload**. Review the category totals before tapping **Import products**. **Manual upload** opens the existing item form.

The importer accepts `.xlsx`, `.xls`, `.xlsm`, and `.csv` files. Put one product on each row with a header row. The only required column is **Product Name**. A **Category** column can contain `Plants`, `Pots`, or `Supplies`; the importer then puts each row in that inventory section. Without a category, a worksheet named Plants, Pots, or Supplies determines it. Otherwise, the section opened before upload is used.

| Column | Also recognized as |
| --- | --- |
| Product Name | Name, Item Name, Plant Name, Pot Name, Supply Name |
| Category | Type, Product Category |
| Quantity | Batch, Qty, Stock, Stock Count |
| Details | Description, Notes |
| Landing Price | Cost Price, Purchase Price, Vendor Price |
| Vendor Name | Vendor, Supplier Name, Supplier |
| Vendor Phone | Vendor Mobile, Supplier Phone, Phone |
| Vendor Email | Supplier Email, Email |
| Photo | Image, Image URL, Photo URL, Image URI |

For product photos in an Excel workbook, insert each image directly over its product's row. The importer saves the embedded image into the admin app and associates it with that row. CSV cannot contain embedded images. A local `content://` photo URI is also accepted when the app has access to it. Web links and filenames in the Photo column are reported in the preview because they are not reliable local image sources.

Blank product names are skipped. Unknown category values are skipped and shown as warnings. Existing products are kept; each imported row becomes a new product.
