# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com),
and this project adheres to [Semantic Versioning](https://semver.org).

## [0.1.0] - 2026-02-28

### Added

- Main menu with navigation to Trip Library, Locations, and Settings
- Trip Library with folder-based organization for GPX and PDF files
- GPX file import with automatic metadata extraction (routes, tracks, waypoints, distance)
- PDF file import with page count detection
- File operations: upload, download, rename, move, copy, delete
- Location Library with folder-based organization
- Location entries with name, address, category, and coordinates
- Category management for locations
- Room database with five tables (folders, gpx_files, pdf_files, locations, categories)
- SAX-based GPX parser with Haversine distance calculation
- Material 3 UI with dark and light theme support
- Unit tests for GPX parser, category repository, and location repository
