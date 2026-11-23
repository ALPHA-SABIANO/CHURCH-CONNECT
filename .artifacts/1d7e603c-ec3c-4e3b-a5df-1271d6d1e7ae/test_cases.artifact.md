# Church Connect - Software Test Case Specification

This document contains the designed test cases for the **Church Connect** Android application, fully compliant with academic software testing specification requirements.

---

## 1. Authentication Module (`AUTH`)

### Feature 1: User Registration (`AUTH_REG`)

#### Test Case ID: `AUTH_REG_001` (Positive)
- **Module & Feature:** `AUTH_REG`
- **Test Title:** Successful User Registration with Valid Details and Role Selection
- **Pre-conditions:** App is launched and user is on the `AuthScreen` (Registration tab).
- **Test Steps:**
  1. Select account type role ("Member").
  2. Enter "Grace Banda" into the Full Name field.
  3. Enter "0991234567" into the Email Address or Phone Number field.
  4. Enter "password123" into the Password field.
  5. Enter "password123" into the Confirm Password field.
  6. Tap "Create Account" button.
- **Expected Result:** Account is created successfully. User is navigated to `HomeScreen` displaying the user's details and role label.

#### Test Case ID: `AUTH_REG_002` (Negative)
- **Module & Feature:** `AUTH_REG`
- **Test Title:** Registration Failure Due to Password Mismatch
- **Pre-conditions:** App is launched and user is on the `AuthScreen` (Registration tab).
- **Test Steps:**
  1. Select account type role ("Member").
  2. Enter "Grace Banda" into the Full Name field.
  3. Enter "0991234567" into the Email Address or Phone Number field.
  4. Enter "password123" into the Password field.
  5. Enter "password999" into the Confirm Password field.
  6. Tap "Create Account" button.
- **Expected Result:** Registration fails. An error message `"Passwords do not match."` is displayed in red text on `AuthScreen`, and user remains on registration page.

---

### Feature 2: User Login (`AUTH_LOG`)

#### Test Case ID: `AUTH_LOG_001` (Positive)
- **Module & Feature:** `AUTH_LOG`
- **Test Title:** Successful Login with Valid Email or Phone Identifier
- **Pre-conditions:** Registered user account exists. User is on the `AuthScreen` (Login tab).
- **Test Steps:**
  1. Enter "0991234567" into the Email Address or Phone Number field.
  2. Enter "password123" into the Password field.
  3. Tap "Log In" button.
- **Expected Result:** Authentication succeeds. User is logged in and redirected to `HomeScreen`.

#### Test Case ID: `AUTH_LOG_002` (Negative)
- **Module & Feature:** `AUTH_LOG`
- **Test Title:** Login Failure Due to Invalid Identifier Format
- **Pre-conditions:** User is on the `AuthScreen` (Login tab).
- **Test Steps:**
  1. Enter "invalid-id-123" into the Email Address or Phone Number field.
  2. Enter "password123" into the Password field.
  3. Tap "Log In" button.
- **Expected Result:** Login fails. Error message `"Please enter a valid email address or phone number."` is displayed on `AuthScreen`.

---

## 2. Church Feed & Announcements Module (`FEED`)

### Feature 1: Announcement Category Filtering (`FEED_ANNC`)

#### Test Case ID: `FEED_ANNC_001` (Positive)
- **Module & Feature:** `FEED_ANNC`
- **Test Title:** Filter Announcements List by Specific Category
- **Pre-conditions:** Announcements exist under categories "Service", "Activity", and "Event". User is on `HomeScreen` (News tab).
- **Test Steps:**
  1. View `HomeScreen` News tab.
  2. Tap "Service" filter chip in the category row.
- **Expected Result:** The list updates to display only announcements categorized as "Service".

#### Test Case ID: `FEED_ANNC_002` (Negative)
- **Module & Feature:** `FEED_ANNC`
- **Test Title:** Category Filter with No Matching Items
- **Pre-conditions:** No announcements exist under category "Activity". User is on `HomeScreen` (News tab).
- **Test Steps:**
  1. View `HomeScreen` News tab.
  2. Tap "Activity" filter chip.
- **Expected Result:** No announcement cards are displayed. Empty state message `"No announcements here yet."` is shown on `HomeScreen`.

---

### Feature 2: Information Posts & Author Attribution (`FEED_INFO`)

#### Test Case ID: `FEED_INFO_001` (Positive)
- **Module & Feature:** `FEED_INFO`
- **Test Title:** Display Information Post with Author Attribution and Timestamp
- **Pre-conditions:** Admin "Pastor Timothy" published an information post. User is on `HomeScreen` (Home tab).
- **Test Steps:**
  1. View `HomeScreen` Home tab.
  2. Locate the information post card (`InfoCard`).
- **Expected Result:** Information card displays title, details, and attribution footer `"Shared by Pastor Timothy • [Date Time]"`.

#### Test Case ID: `FEED_INFO_002` (Negative)
- **Module & Feature:** `FEED_INFO`
- **Test Title:** Display Fallback Default Author for Information Post Without Author Name
- **Pre-conditions:** An information post exists without author metadata. User is on `HomeScreen` (Home tab).
- **Test Steps:**
  1. View `HomeScreen` Home tab.
  2. Locate information post card.
- **Expected Result:** Information card falls back gracefully to display `"Shared by Admin • [Date Time]"`.

---

## 3. Word of Life Module (`WORD`)

### Feature 1: Scheduled Word Publication (`WORD_PUB`)

#### Test Case ID: `WORD_PUB_001` (Positive)
- **Module & Feature:** `WORD_PUB`
- **Test Title:** Display Published Word of Life Post
- **Pre-conditions:** A Word post with `publishAt` timestamp in the past exists. User is on `HomeScreen` (Word tab).
- **Test Steps:**
  1. Open `HomeScreen`.
  2. Tap "Word" navigation tab.
- **Expected Result:** Published Word card (`WordCard`) displays scripture, message, and attribution `"Shared by [Pastor Name] • [Date Time]"`.

#### Test Case ID: `WORD_PUB_002` (Negative)
- **Module & Feature:** `WORD_PUB`
- **Test Title:** Hide Scheduled Word of Life Whose Publication Time Has Not Arrived
- **Pre-conditions:** A Word post with `publishAt` timestamp in the future exists. User is on `HomeScreen` (Word tab).
- **Test Steps:**
  1. Open `HomeScreen`.
  2. Tap "Word" navigation tab.
- **Expected Result:** The future scheduled Word post is hidden from regular members. If no past words exist, empty state `"No words of encouragement yet. Check back soon."` is displayed.

---

## 4. Discussion & Threaded Replies Module (`DISC`)

### Feature 1: Member Question Submission (`DISC_POST`)

#### Test Case ID: `DISC_POST_001` (Positive)
- **Module & Feature:** `DISC_POST`
- **Test Title:** Submit Question or Comment on Announcement Detail
- **Pre-conditions:** User logged in as a Member. On `DetailScreen` for an announcement.
- **Test Steps:**
  1. Tap an announcement card on `HomeScreen`.
  2. Enter "What time does mid-week service start?" in text field ("Write a comment or question...").
  3. Tap Send icon.
- **Expected Result:** Question is saved and displayed in the discussion list on `DetailScreen` with member name and timestamp.

#### Test Case ID: `DISC_POST_002` (Negative)
- **Module & Feature:** `DISC_POST`
- **Test Title:** Prevent Submission of Empty Comment Text
- **Pre-conditions:** User is on `DetailScreen`.
- **Test Steps:**
  1. Leave comment text field empty or fill with spaces.
  2. Observe Send icon state.
- **Expected Result:** Send icon button remains disabled. No blank comment is submitted or added to the discussion list.

---

### Feature 2: Staff Threaded Reply (`DISC_REPLY`)

#### Test Case ID: `DISC_REPLY_001` (Positive)
- **Module & Feature:** `DISC_REPLY`
- **Test Title:** Staff Submits Threaded Reply to Member Question
- **Pre-conditions:** User logged in as Pastor or Admin. On `DetailScreen` containing member question.
- **Test Steps:**
  1. Observe input field placeholder reads `"Write a reply..."`.
  2. Tap "Reply" button on a member's question card.
  3. Observe context banner `"Replying to Grace Banda"` appears.
  4. Enter "Service starts at 6:00 PM." in text field.
  5. Tap Send icon.
- **Expected Result:** Reply is saved with `replyToId`, tagged with badge `"Pastor Reply"`, and displayed nested/indented underneath the member's question.

#### Test Case ID: `DISC_REPLY_002` (Negative)
- **Module & Feature:** `DISC_REPLY`
- **Test Title:** Regular Member Cannot Access Staff Reply Labeling
- **Pre-conditions:** User logged in as regular Member. On `DetailScreen`.
- **Test Steps:**
  1. View input placeholder and comment badges.
- **Expected Result:** Member input placeholder reads `"Write a comment or question..."`. Member comments do not display staff badges ("Pastor Reply" or "Admin Reply").

---

### Feature 3: Comment Deletion Permissions (`DISC_DEL`)

#### Test Case ID: `DISC_DEL_001` (Positive)
- **Module & Feature:** `DISC_DEL`
- **Test Title:** Staff Deletes Any Comment or Member Deletes Own Comment
- **Pre-conditions:** Staff user (Pastor/Admin) is viewing `DetailScreen`.
- **Test Steps:**
  1. Locate target comment card.
  2. Tap red Delete icon.
- **Expected Result:** Comment is deleted and removed from `DetailScreen`.

#### Test Case ID: `DISC_DEL_002` (Negative)
- **Module & Feature:** `DISC_DEL`
- **Test Title:** Regular Member Cannot Delete Another Member's Comment
- **Pre-conditions:** Regular member viewing a comment written by a different user.
- **Test Steps:**
  1. View comment card written by another member.
  2. Check for Delete icon.
- **Expected Result:** Delete icon is hidden for another member's comment (`canDeleteComment` returns false).

---

## 5. Attendance Module (`ATT`)

### Feature 1: Service Attendance Summary Calculation (`ATT_REC`)

#### Test Case ID: `ATT_REC_001` (Positive)
- **Module & Feature:** `ATT_REC`
- **Test Title:** Calculate Service Attendance Percentage and Summary Format
- **Pre-conditions:** 40 members present out of total 50 members.
- **Test Steps:**
  1. Pass present count (40) and total count (50) to `formatAttendanceSummary`.
- **Expected Result:** Returns formatted summary string `"80% (40 of 50 present)"`.

#### Test Case ID: `ATT_REC_002` (Negative)
- **Module & Feature:** `ATT_REC`
- **Test Title:** Attendance Summary Calculation with Zero Total Members
- **Pre-conditions:** Total member count is 0.
- **Test Steps:**
  1. Pass present count (0) and total count (0) to `formatAttendanceSummary`.
- **Expected Result:** Division by zero is prevented and fallback string `"0% (0 present)"` is returned.
