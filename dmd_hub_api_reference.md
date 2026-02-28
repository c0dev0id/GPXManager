# DMD HUB API Reference

> Reverse-engineered from HAR session recording. This is an unofficial, undocumented API.

---

## Authentication

Authentication uses standard **session cookies** set by a form-based login. Two cookies are issued on successful login and must be sent with all subsequent requests.

### Login

First, load the login page to obtain the CSRF token from the HTML, then POST credentials:

```
GET https://hub.dmdnavigation.com/account/login/
→ Scrape csrf_token from the page HTML
```

```
POST https://hub.dmdnavigation.com/account/login/
Content-Type: application/x-www-form-urlencoded

csrf_token   = <scraped from page>
email        = you@example.com
password     = yourpassword
remember     = on
submit_login = submit_login
```

### Cookies set on successful login

| Cookie | Domain | HttpOnly | Secure | MaxAge | Purpose |
|---|---|---|---|---|---|
| `dmdub_session` | `.dmdnavigation.com` | ✓ | ✓ | 30 days | Session identifier |
| `dmdub_remember` | `.dmdnavigation.com` | ✓ | ✓ | 30 days | Persistent remember-me token |

Both cookies must be sent with all subsequent requests. The `dmdub_remember` cookie alone is sufficient for authenticated API calls when the session has expired — it will re-establish the session automatically.

**Example cookie header:**
```
Cookie: dmdub_session=fcebb6b3ad14bf5afd25b214ba07a3d8; dmdub_remember=23782ee24601dea232%3Af7614bd...
```

### CSRF tokens

HTML form POSTs (GPX edit/upload, location create/edit/delete) require a `csrf_token` field. This token is embedded in every authenticated page as a hidden input:

```html
<input type="hidden" name="csrf_token" value="4371b11c755223c06b4078d0b542423a...">
```

The token is **session-scoped** — scrape it once from any page after login and reuse it for all form submissions in that session. JSON API calls (`/api/gpx-manager.php` etc.) do **not** require a CSRF token.

### Profile details update

Social links and other profile fields can be updated via:

```
POST https://hub.dmdnavigation.com/account/profile/details/
Content-Type: application/x-www-form-urlencoded

csrf_token                  = <token>
facebook_url                = ""
instagram_url               = "https://instagram.com/yourhandle"
twitter_url                 = ""
youtube_url                 = "https://www.youtube.com/@yourchannel"
submit_udpate_social_links  = submit_udpate_social_links
```

**Response:** HTML (not JSON).

---

## Base URL

```
https://hub.dmdnavigation.com/api/
```

---

## GPX Manager (`/api/gpx-manager.php`)

The main API for managing personal GPX files and folders.

### List files and folders

Returns the user's top-level folders and files (not in any folder).

```
GET /api/gpx-manager.php?action=list
```

Optional parameter:
- `recursive=1` — includes files nested in subfolders (adds `folder_id` and `folder_path` to file objects)

**Response:**
```json
{
  "success": true,
  "folders": [
    {
      "id": 784,
      "user_id": "6863abf38c6263fc3c0b6192",
      "parent_id": null,
      "name": "Adventure Country Tracks (ACT)",
      "icon": "default",
      "created_at": "2026-02-13 12:17:01",
      "folder_count": 0,
      "file_count": 8
    }
  ],
  "files": [
    {
      "_id": "69899de9187aca70510938b5",
      "owner": "6863abf38c6263fc3c0b6192",
      "public": false,
      "allow_download": false,
      "allow_index": true,
      "approved": false,
      "file": "69899de9d881d.gpx",
      "show_on_map": false,
      "title": "Test Round",
      "continent": "Europe",
      "country": "Germany",
      "best_time": [],
      "vehicle": [],
      "difficulty": "Easy",
      "off_road_percentage": 0,
      "description": "",
      "warnings": "",
      "image1_url": "",
      "image2_url": "",
      "youtube": "",
      "tags": "Test",
      "rating": null,
      "rating_amount": null,
      "_state": 1,
      "_modified": 1771446669,
      "_created": 1770626537,
      "file_path": "/storage/users/6863abf38c6263fc3c0b6192/gpx_files/69899de9d881d.gpx",
      "gpx_length_km": 102.75,
      "gpx_tracks_count": 1,
      "gpx_routes_count": 0,
      "gpx_waypoints_count": 0,
      "color": "",
      "gpx_meta_time": "2026-02-09T08:39:05.537Z",
      "grid1": "1000_15_13",
      "grid2": "500_31_27",
      "grid3": "250_62_54",
      "grid4": "100_155_136"
    }
  ]
}
```

When `recursive=1`, file objects also include:
```json
{
  "folder_id": 15,
  "folder_path": "Bike Coaching / 2026-05 Pässe"
}
```

---

### Get GPX file details

Returns full metadata and parsed track/route/waypoint data for a single GPX file.

```
GET /api/gpx-manager.php?action=get_gpx_info&gpx_id={_id}
```

**Response:**
```json
{
  "success": true,
  "gpx": {
    "_id": "69a2af6ce18261d68a0dbcd2",
    "owner": "6863abf38c6263fc3c0b6192",
    "public": false,
    "allow_download": false,
    "allow_index": true,
    "approved": false,
    "file": "69a2af6cc7b6c.gpx",
    "show_on_map": false,
    "title": "Mayen Tour Tag 2",
    "continent": "Europe",
    "country": "Germany",
    "best_time": [],
    "vehicle": [],
    "difficulty": "Medium",
    "off_road_percentage": 50,
    "description": "",
    "warnings": "",
    "image1_url": "",
    "image2_url": "",
    "youtube": "",
    "tags": "",
    "_state": 1,
    "_modified": 1772269420,
    "_created": 1772269420
  },
  "parsed": {
    "valid": true,
    "name": "",
    "description": "test",
    "author": "Scenic Motorcycle Navigation App",
    "tracks": [
      {
        "name": "Mayen Tour (Track)",
        "description": "...",
        "points_count": 8840,
        "length_km": 205.3,
        "points": [
          { "lat": 50.328583, "lon": 7.23287, "ele": 259, "time": "2025-05-22T06:03:30.000Z" }
        ]
      }
    ]
  }
}
```

---

### Delete GPX file

```
POST /api/gpx-manager.php
Content-Type: application/json

{
  "action": "delete",
  "gpx_id": "69a2af6ce18261d68a0dbcd2"
}
```

**Response:**
```json
{ "success": true }
```

---

### Upload GPX file (inferred)

Based on the UI behavior (upload form), the upload likely uses a multipart POST:

```
POST /api/gpx-manager.php
Content-Type: multipart/form-data

action=upload
file=<gpx file binary>
title=<string>
folder_id=<int>           (optional)
```

*Note: This specific request was not captured in the HAR — the exact parameter names may differ.*

---

### Rename GPX file

```
POST /api/gpx-manager.php
Content-Type: application/json

{ "action": "rename", "gpx_id": "699479a2c01ee8e2d90b01a2", "title": "New Title" }
```

**Response:**
```json
{ "success": true }
```

---

### Create folder

```
POST /api/gpx-manager.php
Content-Type: application/json

{ "action": "create_folder", "name": "My Folder", "parent_id": null }
```

Use a numeric folder ID for `parent_id` to create a subfolder, or `null` for a root-level folder.

**Response:**
```json
{
  "success": true,
  "folder": {
    "id": 1486,
    "user_id": "6863abf38c6263fc3c0b6192",
    "parent_id": null,
    "name": "My Folder",
    "folder_count": 0,
    "file_count": 0,
    "is_community_subfolder": false
  }
}
```

---

### Rename folder

```
POST /api/gpx-manager.php
Content-Type: application/json

{ "action": "rename_folder", "folder_id": "1486", "name": "New Name" }
```

**Response:**
```json
{ "success": true }
```

---

### Delete folder

```
POST /api/gpx-manager.php
Content-Type: application/json

{ "action": "delete_folder", "folder_id": "1486" }
```

**Response:**
```json
{ "success": true }
```

---

### Get folder details

```
GET /api/gpx-manager.php?action=get_folder_details&folder_id=1486
```

**Response:**
```json
{
  "success": true,
  "folder": {
    "id": 1486,
    "user_id": "6863abf38c6263fc3c0b6192",
    "parent_id": null,
    "name": "test 123",
    "icon": "default",
    "created_at": "2026-02-28 09:16:30"
  }
}
```

Returns `{ "success": false, "error": "Folder ID is required" }` if `folder_id=null`.

---

### Get folder breadcrumb path

```
GET /api/gpx-manager.php?action=get_folder_path&folder_id=1486
```

**Response:**
```json
{
  "success": true,
  "path": [
    { "id": 1486, "name": "test 123" }
  ]
}
```

For nested folders, the array contains each ancestor in order from root to the requested folder.

---

### List contents of a specific folder

```
GET /api/gpx-manager.php?action=list&folder_id=1486
```

**Response:**
```json
{
  "success": true,
  "folders": [],
  "files": [],
  "current_folder": 1486,
  "search_mode": false,
  "folders_with_matches": [],
  "has_filters": false
}
```

---

### Search / filter by tag

```
GET /api/gpx-manager.php?action=list&recursive=1&tags=Test
```

Filters the file listing to only those matching the given tag. The `search_mode` flag in the response will be `true` and `has_filters` will be `true` when filters are active.

---

### Set GPX color

```
POST /api/gpx-manager.php
Content-Type: application/json

{ "action": "set_color", "gpx_id": "69a2b33bd40f53321a0a7143", "color": "green" }
```

**Response:**
```json
{ "success": true }
```

Known color values: `"green"`, `"red"`, `"blue"`, `""` (clear). Likely others exist.

---

### Move file or folder

```
POST /api/gpx-manager.php
Content-Type: application/json

{ "action": "move_item", "item_type": "file", "item_id": "69a2b33bd40f53321a0a7143", "target_folder_id": 14 }
```

- `item_type`: `"file"` or `"folder"`
- `target_folder_id`: numeric folder ID, or likely `null` to move to root

**Response:**
```json
{ "success": true }
```

---

## GPX Download (`/api/gpx-download/`)

Returns the raw GPX XML content for a given file. Authenticated.

```
GET /api/gpx-download/?id={gpx_id}
```

**Response:** `application/gpx+xml` — the raw GPX file content.

This differs from the `/storage/` path in that it goes through the API and respects permissions.

---

## GPX Upload (`/account/profile/gpx/add/`)

GPX upload uses a standard HTML form POST. A CSRF token is required (obtained from the add page HTML).

```
POST /account/profile/gpx/add/
Content-Type: multipart/form-data

csrf_token        = <token from page HTML>
submit_new_gpx    = 1
gpx_file          = <file upload, filename="yourfile.gpx", Content-Type: application/gpx+xml>
```

**Response:** HTML redirect back to the GPX manager (not JSON). Success is inferred from a 200 + redirect.

---

## GPX Edit (`/account/profile/gpx/edit/`)

Full metadata edit for an existing GPX file. Also a CSRF-protected form POST.

```
POST /account/profile/gpx/edit/?id={gpx_id}
Content-Type: multipart/form-data

csrf_token            = <token from page HTML>
submit_edit_gpx       = 1
gpx_file              = <optional replacement GPX file upload, or empty>
title                 = "My Route Title"
description           = ""
warnings              = ""
continent             = "Europe"
country               = "Germany"
difficulty            = 2           (1=Easy, 2=Medium, 3=Hard — numeric)
off_road_percentage   = 50
tags                  = "tag1,tag2"
allow_index           = "true"
gpx_meta_description  = ""
gpx_meta_link         = "https://scenic.app"
gpx_meta_author       = "Scenic Motorcycle Navigation App"
gpx_meta_time         = ""
youtube               = ""
image1                = <optional image upload, or empty>
image2                = <optional image upload, or empty>
```

**Response:** HTML redirect (not JSON). Best_time and vehicle fields were not present in the captured form — they may be set elsewhere or default to empty arrays.

**Difficulty values:**
- `1` = Easy
- `2` = Medium
- `3` = Hard

---

## Locations (`/account/profile/locations/`)

Locations are managed via classic HTML form POSTs, not the JSON API. All require a CSRF token from the page.

### Create location

```
POST /account/profile/locations/
Content-Type: multipart/form-data

csrf_token          = <token from page HTML>
submit_new_location = submit_new_location
title               = "My Location"
continent_specific  = "Europe"
country_specific    = "Germany"
coordinates         = "49.310519, 8.558673"   (lat, lon as string)
address             = "Street Name, City"
category[]          = "Other (will not be public)"
main_category       = ""
ground              = ""
crowded             = ""
tags                = "test"
short_description   = "Short text"
description         = ""
warnings            = ""
website             = ""
phone               = ""
youtube_link        = ""
image_primary       = <optional image upload>
image_secondary     = <optional image upload>
```

**Response:** HTML redirect. Location ID assigned by server.

---

### Edit location

```
POST /account/profile/locations/edit/{location_id}
Content-Type: multipart/form-data

csrf_token              = <token from page HTML>
submit_edit_locations   = submit_edit_locations
image_primary_path      = "/storage/users/{uid}/locations/existing.jpg"   (existing image path)
title                   = "Updated Title"
show_on_map             = "true"
continent_specific      = "Europe"
country_specific        = "Germany"
coordinates             = "49.470371, 8.606765"
address                 = "Dr-Carl-Benz-Platz 5,\nLadenburg"
category[]              = "Other (will not be public)"
main_category           = ""
ground                  = ""
crowded                 = ""
tags                    = "Test"
short_description       = ""
description             = ""
warnings                = ""
website                 = ""
phone                   = ""
youtube_link            = ""
image_primary           = <optional replacement image upload, or empty>
image_secondary         = <optional replacement image upload, or empty>
```

**Response:** HTML redirect.

---

### Toggle show_on_map

```
POST /account/profile/locations/
Content-Type: application/x-www-form-urlencoded

csrf_token              = <token>
_id                     = 69a2b3856262f6dd300d9553
submit_toggle_show_on_map = 1
current_show_on_map     = 0
show_on_map             = 1
```

---

### Delete location

```
POST /account/profile/locations/
Content-Type: application/x-www-form-urlencoded

csrf_token                      = <token>
_id                             = 69a2b3856262f6dd300d9553
submit_change_location_delete   = 1
```

---

### Location filter preference (continent/country)

These POSTs save the user's current filter selection for the locations page — not data mutations.

```
POST /account/profile/locations/add          (or /edit/{id})
Content-Type: application/x-www-form-urlencoded

save_location_preference = 1
continent_specific       = "Europe"
country_specific         = "Germany"         (or empty string for "all countries")
```

---

## Nominatim Geocoding Proxy (`/core/helpers/locations/nominatim_proxy.php`)

The site proxies Nominatim (OSM geocoding) through its own backend. No separate auth beyond the session cookie.

### Forward geocoding (search)

```
POST /core/helpers/locations/nominatim_proxy.php
Content-Type: application/json

{ "query": "gustav-stresemann-weg, hockenheim", "limit": 5 }
```

**Response:** Array of Nominatim place objects with `place_id`, `lat`, `lon`, `display_name`, `boundingbox`, etc.

### Reverse geocoding

```
POST /core/helpers/locations/nominatim_proxy.php
Content-Type: application/json

{ "action": "reverse", "lat": 49.5042, "lon": 8.474379, "zoom": 5 }
```

**Response:** Single Nominatim place object with `name`, `display_name`, `address`, `boundingbox`.

---

## CSRF Tokens

The locations and GPX edit/add forms all require a `csrf_token` field. This token must be scraped from the HTML of the relevant page before submitting. It appears as a hidden form field and is consistent across all forms in a single session:

```html
<input type="hidden" name="csrf_token" value="55b69208e6d3...">
```

The token is session-scoped — obtain it once per session by loading any of the form pages.

---

## GPX Collection (`/api/gpx-collection/`)

Community-shared GPX file collection (public routes from all users).

### List collection

```
GET /api/gpx-collection/?action=list
```

**Response:**
```json
{
  "success": true,
  "files": [
    {
      "_id": "69995471b5151f16980dde06",
      "owner": "68309ba97bb5d2a229091592",
      "public": true,
      "allow_download": false,
      "allow_index": true,
      "approved": true,
      "file": "699954713c3a8.gpx",
      "show_on_map": true,
      "title": "Alpine Passes in Europe",
      "continent": "Europe",
      "country": "Switzerland",
      "best_time": ["Spring", "Summer", "Autumn"],
      "vehicle": ["Enduro Motorcycle", "Adventure Motorcycle", "4x4 Car / Pickup"],
      "difficulty": "Medium",
      "off_road_percentage": 20,
      "description": "...",
      "warnings": "...",
      "owner_name": "silberfuchs",
      "gpx_length_km": 475.35,
      "gpx_tracks_count": 1,
      "gpx_routes_count": 0,
      "gpx_waypoints_count": 5258,
      "file_path": "/storage/users/.../gpx_files/699954713c3a8.gpx"
    }
  ]
}
```

---

## Downloading GPX Files

Raw GPX files are served from the `/storage/` path, not the API:

```
GET https://hub.dmdnavigation.com{file_path}?v={_modified}
```

Example:
```
GET https://hub.dmdnavigation.com/storage/users/6863abf38c6263fc3c0b6192/gpx_files/69a2af6cc7b6c.gpx?v=1772269420
```

The `?v=` parameter is a cache-buster (the `_modified` unix timestamp from the file metadata).

---

## Notification Endpoints

Polled periodically by the frontend (with `_t=<timestamp>` cache-buster).

```
GET /api/chat-notifications?action=check
→ { "success": true, "total_unread": 0, "groups": [] }

GET /api/following-activity-notifications?action=check&_t=1772269424275
→ { "success": true, "new_count": 0, "has_following": true }

GET /api/hub-feed-notifications?action=check&_t=1772269425275
→ { "success": true, "new_count": 0 }
```

---

## External Routing API (`router.advhub.net`)

The planner uses a BRouter instance hosted by DMD/advhub for route calculation:

```
GET https://router.advhub.net/api/brouter
  ?profile=road-fast-all
  &format=geojson
  &lonlats=7.232881,50.328587|7.22459,50.346292|...
```

Parameters:
- `profile` — routing profile (e.g. `road-fast-all`, likely others like `trekking`, `enduro`)
- `format` — `geojson` or `gpx`
- `lonlats` — pipe-separated `lon,lat` waypoints

**Response:** GeoJSON FeatureCollection with BRouter 1.7.8 properties including track-length, ascent, time, energy, and detailed per-segment messages.

This endpoint appears to be **public** (no auth cookie required).

---

## Data Model Notes

### GPX File Object Fields

| Field | Type | Notes |
|---|---|---|
| `_id` | string | MongoDB ObjectId |
| `owner` | string | User ID |
| `public` | bool | Visible in community |
| `allow_download` | bool | Others can download |
| `allow_index` | bool | Show in search index |
| `approved` | bool | Staff-approved |
| `file` | string | Stored filename |
| `file_path` | string | Full storage path |
| `title` | string | Display name |
| `continent` | string | e.g. "Europe" |
| `country` | string | e.g. "Germany" |
| `difficulty` | string | "Easy", "Medium", "Hard" |
| `off_road_percentage` | int | 0–100 |
| `best_time` | array | e.g. `["Spring", "Summer"]` |
| `vehicle` | array | e.g. `["Enduro Motorcycle"]` |
| `gpx_length_km` | float | Calculated route length |
| `gpx_tracks_count` | int | Number of `<trk>` elements |
| `gpx_routes_count` | int | Number of `<rte>` elements |
| `gpx_waypoints_count` | int | Number of `<wpt>` elements |
| `color` | string | Display color (hex or empty) |
| `grid1`–`grid4` | string | Tile grid references |
| `_created` / `_modified` | int | Unix timestamps |

### Folder Object Fields

| Field | Type | Notes |
|---|---|---|
| `id` | int | MySQL integer ID |
| `user_id` | string | Owner user ID |
| `parent_id` | int or null | Parent folder (null = root) |
| `name` | string | Folder name |
| `icon` | string | e.g. "default" |
| `folder_count` | int | Subfolder count |
| `file_count` | int | GPX files in this folder |
