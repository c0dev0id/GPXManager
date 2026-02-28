# GPX Manager Specification

The GPX Manager is an Android Application, which serves as library for GPX and PDF Files and Location data.

**Main Screen**
The Main Screen shows a Menu with the following entries:
- Trip Library
- Locations
- Settings

**Trip Library**
The trip library can contain the following file types:
- folders
- gpx files
- pdf files

Actions on those file types:
- create: folders
- delete: folders, gpx files, pdf files
- rename, move, copy: folders, gpx files, pdf files
- download: gpx files, pdf files
- upload: gpx files, pdf files

When files are uploaded, they should be stored in the current folder.

GPX Files and PDF Files should be shown as cards.
- left 1/3 card should contain a preview. We will add this later.
- right 2/3 card should contain file details
  - For GPX Files:
    - Name
    - Date
    - Number of routes, tracks and waypoints in the file
    - Combined (routes+tracks) length in km
  - For PDF Files
    - Name
    - Upload Date
    - Number of Pages

**Location Library**

The location library can contain the following file types:
- folders
- locations

Actions on those file types:
- create: folders, locations
- delete: folders, locations
- rename, move, copy: folders, locations

Locations should be displayed as cards.
- left 1/3 card should contain a preview. We will add this later.
- right 2/3 card should contain location details:
  - Location Name
  - Location Address

When the user opens a Location, he can maintain:
- Location Name
- Location Address
- Location Category (drop down list) + New Button: Asks for a name and creates a category with this name.
- Coordinates: <lon>,<lat>

We will later add a mechanism to get to the Coordinates from the Address.
