# Campus Lost & Found System

## Introduction

Campus Lost & Found is a Java desktop application for reporting items lost or found on campus. It provides a Swing dashboard, searchable reports, and local file storage.

## Problem Statement

Students and staff can have difficulty reporting misplaced belongings and finding the person who has recovered an item. This project keeps lost and found reports in one place so they can be searched and their status can be updated.

## Project Objectives

- Provide a simple desktop interface for lost and found reports.
- Store reports locally so they are available after restarting the application.
- Let users search and filter reports.
- Track when a found item has been marked as recovered.
- Demonstrate Java object-oriented programming and file handling.

## Features

- Dashboard with counts for lost, found, and recovered reports.
- Lost item form with item name, category, description, last seen location, date, student name, and contact number.
- Found item form with item name, category, description, found location, date, finder name, and contact number.
- Required-field and contact-number validation on both report forms.
- Search and filter window with a table of saved reports.
- Filters for item name, category, lost or found type, and recovered status.
- Ability to mark an unrecovered found report as recovered.
- Clear and cancel actions on report forms.
- Local persistence in `data/items.txt`.
- Damaged records are skipped during loading and reported to the console. File access failures are handled as storage errors.

## Technologies Used

- Java
- Java Swing and AWT for the desktop interface
- Java collections, including `ArrayList`
- Java NIO file handling

No external frameworks are required.

## Project Structure

```text
CampusLostAndFound/
├── src/
│   └── campuslostfound/
│       ├── Main.java
│       ├── Item.java
│       ├── LostItem.java
│       ├── FoundItem.java
│       └── ItemManager.java
├── data/
│   └── items.txt
├── out/                    # Compiled class files
└── README.md
```

## Java Concepts Used

- **Abstraction:** `Item` is an abstract base class that defines shared item data and abstract type and status methods.
- **Encapsulation:** Item details are held in private fields and accessed through getters.
- **Inheritance:** `LostItem` and `FoundItem` extend `Item`.
- **Polymorphism:** The application handles different item subclasses through the common `Item` type; subclasses provide their own type and status behavior.
- **Interface:** `ItemManager` implements the `ItemRepository` interface for adding, loading, and searching reports.

## File Storage

Reports are stored in `data/items.txt` by the existing `ItemManager`. Each report is written as a tab-separated record; field values are Base64-encoded UTF-8 so tabs, line breaks, and other special characters do not break the record structure. New reports are appended to the file. Marking a found report as recovered updates that report while preserving other file lines.

The reader supports the earlier seven-field record format as well as the current expanded format. Empty files load as an empty report list. Malformed rows are skipped and reported to the console rather than stopping the loading of all other reports.

## Compile and Run

Run these commands from the project root in PowerShell or a terminal with a JDK installed:

```powershell
javac -encoding UTF-8 -d out src\campuslostfound\*.java
java -cp out campuslostfound.Main
```

## How to Use the Application

1. Start the application using the commands above.
2. Use the left navigation or dashboard actions to open a report form.
3. Enter the required item and contact details, then submit. Use **Clear Form** to reset the fields or **Cancel** to close without saving.
4. Open **Search Items** to view saved reports. Search by item name and apply category, type, or recovery-status filters.
5. Select an unrecovered found report and choose **Mark Selected as Recovered** to update its status.
6. Return to the dashboard to see the counts refreshed from the saved records.

## Testing Completed

- The full Java source set compiled successfully with `javac`.
- A file-storage audit using a temporary copy verified saving lost and found reports, marking a report recovered, reloading records, ID uniqueness checks, special-character handling, legacy record loading, empty-file loading, and tolerance of a damaged row.
- The real `data/items.txt` was not used for test submissions.
- A full interactive GUI test was attempted on a temporary project copy but did not complete because the automation stalled at a validation alert. The Swing form, filter, and restart workflows therefore still need a manual end-to-end check.