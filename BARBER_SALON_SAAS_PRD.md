# BARBER & SALON MANAGEMENT SAAS — MASTER PRD
> AI IDE Implementation Guide (Cursor / Windsurf / Copilot)
> Read this file before writing ANY code. Follow phases strictly. One phase at a time.

---

## HOW TO USE THIS DOCUMENT

1. **Before each phase** → Read the phase section fully
2. **Before each module** → Read the module spec fully
3. **Never skip phases** → Each phase depends on the previous
4. **Token efficiency** → Only load the current phase section into context
5. **After each phase** → Run tests, commit, then move to next phase

---

## STACK REFERENCE (Do not deviate)

```
Backend:  Java 21 | Spring Boot 3.x | Spring Security | JWT | Spring Data JPA
DB:       PostgreSQL | Flyway migrations | Redis (Phase 8+)
Frontend: React 19 | Vite | Material UI | React Query | React Hook Form | Zod | Axios
Infra:    Docker | Docker Compose
```

---

## CODEBASE ANALYSIS CHECKLIST
> Run this FIRST before implementing anything

```
[ ] Check existing package structure — identify base packages
[ ] Check existing entity base classes — look for AuditEntity, BaseEntity
[ ] Check existing SecurityConfig — note existing JWT filter chain
[ ] Check existing Role/Permission enums — do not redefine
[ ] Check existing Flyway migration numbering — continue sequence
[ ] Check existing exception handlers — extend, do not replace
[ ] Check existing DTOs/response wrappers — reuse ApiResponse<T> pattern
[ ] Check existing frontend routing — note protected route pattern
[ ] Check existing Axios config — note base URL and interceptors
[ ] Identify existing auth endpoints: /api/auth/register, /login, /refresh, /forgot-password, /reset-password
[ ] Identify existing RBAC: roles table, permissions table, role_permissions join
[ ] Identify existing dashboard: summary cards + charts already wired
```

---

## GLOBAL CONVENTIONS (Apply to every phase)

### Backend Conventions
```java
// Package structure
com.{project}.{module}.controller
com.{project}.{module}.service
com.{project}.{module}.repository
com.{project}.{module}.entity
com.{project}.{module}.dto.request
com.{project}.{module}.dto.response
com.{project}.{module}.mapper
com.{project}.{module}.exception

// Every entity extends BaseEntity (already exists)
// BaseEntity contains: id, shopId, createdAt, createdBy, updatedAt, updatedBy, deletedAt, deletedBy, isDeleted

// Every response wraps ApiResponse<T> (already exists)
// Every service method is @Transactional
// Use MapStruct for entity↔DTO mapping
// Soft delete only — never hard delete business data
```

### Database Conventions
```sql
-- Every business table must have:
shop_id         UUID NOT NULL REFERENCES shop(id)
created_at      TIMESTAMP NOT NULL DEFAULT now()
created_by      UUID REFERENCES users(id)
updated_at      TIMESTAMP
updated_by      UUID REFERENCES users(id)
deleted_at      TIMESTAMP
deleted_by      UUID REFERENCES users(id)
is_deleted      BOOLEAN NOT NULL DEFAULT false

-- Flyway file naming: V{next_number}__{snake_case_description}.sql
-- Always add indexes on: shop_id, is_deleted, foreign keys
```

### Frontend Conventions
```typescript
// File structure per feature
src/features/{feature}/
  components/     UI components
  hooks/          React Query hooks
  api.ts          Axios calls
  types.ts        TypeScript interfaces
  schema.ts       Zod validation schemas
  index.tsx       Page entry

// Always use React Query for server state
// Always use React Hook Form + Zod for forms
// Always use MUI components — no raw HTML forms
// Always handle loading, error, empty states
```

---

## PHASE 0 — FOUNDATION (ALREADY COMPLETED)
> ⚠️ DO NOT RE-IMPLEMENT. Verify existence only.

**Verify these exist before starting Phase 1:**
- [ ] `/api/auth/register` → POST
- [ ] `/api/auth/login` → POST (returns accessToken + refreshToken)
- [ ] `/api/auth/refresh` → POST
- [ ] `/api/auth/forgot-password` → POST
- [ ] `/api/auth/reset-password` → POST
- [ ] Role CRUD endpoints
- [ ] Permission CRUD endpoints
- [ ] RBAC assignment endpoints
- [ ] Dashboard summary endpoint
- [ ] Dashboard chart endpoints

**If any are missing, implement missing items only before Phase 1.**

---

## PHASE 1 — SHOP MANAGEMENT
> Priority: CRITICAL | Estimated: 3-4 days

### 1.1 Flyway Migration

```sql
-- V{N}__create_shop_table.sql
CREATE TABLE shop (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id        UUID NOT NULL REFERENCES users(id),
    name            VARCHAR(255) NOT NULL,
    description     TEXT,
    logo_url        VARCHAR(500),
    cover_image_url VARCHAR(500),
    phone           VARCHAR(20),
    email           VARCHAR(255),
    website         VARCHAR(500),
    -- Location
    address         VARCHAR(500),
    city            VARCHAR(100),
    state           VARCHAR(100),
    country         VARCHAR(100) DEFAULT 'India',
    postal_code     VARCHAR(20),
    latitude        DECIMAL(10, 8),
    longitude       DECIMAL(11, 8),
    -- Verification
    verification_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    -- Audit
    created_at      TIMESTAMP NOT NULL DEFAULT now(),
    created_by      UUID REFERENCES users(id),
    updated_at      TIMESTAMP,
    updated_by      UUID REFERENCES users(id),
    deleted_at      TIMESTAMP,
    deleted_by      UUID REFERENCES users(id),
    is_deleted      BOOLEAN NOT NULL DEFAULT false
);

-- V{N+1}__create_shop_hours_table.sql
CREATE TABLE shop_hours (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    shop_id     UUID NOT NULL REFERENCES shop(id),
    day_of_week VARCHAR(10) NOT NULL,  -- MONDAY..SUNDAY
    opening_time TIME,
    closing_time TIME,
    is_holiday  BOOLEAN NOT NULL DEFAULT false,
    is_deleted  BOOLEAN NOT NULL DEFAULT false
);

CREATE INDEX idx_shop_owner ON shop(owner_id);
CREATE INDEX idx_shop_verification ON shop(verification_status);
CREATE INDEX idx_shop_location ON shop(city, state, is_deleted);
CREATE INDEX idx_shop_hours_shop ON shop_hours(shop_id);
```

### 1.2 Entities

```java
// Shop.java — extend BaseEntity
@Entity @Table(name = "shop")
public class Shop extends BaseEntity {
    @ManyToOne @JoinColumn(name = "owner_id")
    private User owner;
    private String name;
    private String description;
    private String logoUrl;
    private String coverImageUrl;
    private String phone;
    private String email;
    private String website;
    private String address;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private BigDecimal latitude;
    private BigDecimal longitude;
    @Enumerated(EnumType.STRING)
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;
    @OneToMany(mappedBy = "shop", cascade = CascadeType.ALL)
    private List<ShopHours> shopHours;
}

// VerificationStatus enum: PENDING, APPROVED, REJECTED

// ShopHours.java
@Entity @Table(name = "shop_hours")
public class ShopHours {
    @Id @GeneratedValue private UUID id;
    @ManyToOne @JoinColumn(name = "shop_id") private Shop shop;
    @Enumerated(EnumType.STRING) private DayOfWeek dayOfWeek;
    private LocalTime openingTime;
    private LocalTime closingTime;
    private boolean isHoliday;
    private boolean isDeleted;
}
```

### 1.3 API Endpoints

```
POST   /api/shops                     → Create shop (OWNER role)
GET    /api/shops/{id}                → Get shop by ID
PUT    /api/shops/{id}                → Update shop
DELETE /api/shops/{id}                → Soft delete shop
GET    /api/shops/my                  → Get current owner's shops
PUT    /api/shops/{id}/hours          → Update shop hours (full replace)
GET    /api/shops/{id}/hours          → Get shop hours

ADMIN ONLY:
GET    /api/admin/shops               → List all shops (paginated, filterable)
PUT    /api/admin/shops/{id}/verify   → Approve/Reject shop
```

### 1.4 Security Rules
```
OWNER can only CRUD their own shops — enforce with @PreAuthorize + ownership check
ADMIN can view and verify all shops
Public GET for approved shops only
```

### 1.5 Frontend Pages
```
/dashboard/shops           → Shop list page (owner view)
/dashboard/shops/create    → Create shop form
/dashboard/shops/{id}/edit → Edit shop form (tabs: Info | Hours | Location)
/admin/shops               → Admin shop list with verify actions
```

### 1.6 Frontend Form Fields
```
Tab 1 - Basic Info:
  name* | description | phone | email | website
  logo upload | cover image upload

Tab 2 - Location:
  address* | city* | state* | country | postalCode
  Map component (Google Maps or Leaflet):
    - Click to drop pin
    - Drag pin to adjust
    - Auto-fill lat/lng from pin position

Tab 3 - Hours:
  For each day (Mon-Sun):
    [isHoliday toggle] [openingTime picker] [closingTime picker]
```

---

## PHASE 2 — BRANCH MANAGEMENT
> Priority: HIGH | Estimated: 2 days
> Depends on: Phase 1 complete

### 2.1 Flyway Migration

```sql
-- V{N}__create_branch_table.sql
CREATE TABLE branch (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    shop_id     UUID NOT NULL REFERENCES shop(id),
    name        VARCHAR(255) NOT NULL,
    phone       VARCHAR(20),
    email       VARCHAR(255),
    address     VARCHAR(500),
    city        VARCHAR(100),
    state       VARCHAR(100),
    latitude    DECIMAL(10,8),
    longitude   DECIMAL(11,8),
    is_active   BOOLEAN NOT NULL DEFAULT true,
    -- audit columns (same as shop)
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    created_by UUID, updated_at TIMESTAMP, updated_by UUID,
    deleted_at TIMESTAMP, deleted_by UUID, is_deleted BOOLEAN DEFAULT false
);
CREATE INDEX idx_branch_shop ON branch(shop_id, is_deleted);
```

### 2.2 API Endpoints

```
POST   /api/shops/{shopId}/branches          → Create branch
GET    /api/shops/{shopId}/branches          → List branches
GET    /api/shops/{shopId}/branches/{id}     → Get branch
PUT    /api/shops/{shopId}/branches/{id}     → Update branch
DELETE /api/shops/{shopId}/branches/{id}     → Soft delete
PUT    /api/shops/{shopId}/branches/{id}/toggle-active → Enable/Disable
```

### 2.3 Notes
- Branch inherits shop's services and staff (assigned in later phases)
- Branch has its own hours (reuse ShopHours pattern with branch_id)
- Frontend: sub-page under shop detail

---

## PHASE 3 — SERVICE MANAGEMENT
> Priority: CRITICAL | Estimated: 2 days
> Depends on: Phase 1 complete

### 3.1 Flyway Migration

```sql
-- V{N}__create_service_tables.sql
CREATE TABLE service_category (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    shop_id     UUID NOT NULL REFERENCES shop(id),
    name        VARCHAR(100) NOT NULL,
    description TEXT,
    sort_order  INT DEFAULT 0,
    is_deleted  BOOLEAN DEFAULT false
);

CREATE TABLE service (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    shop_id         UUID NOT NULL REFERENCES shop(id),
    category_id     UUID REFERENCES service_category(id),
    name            VARCHAR(255) NOT NULL,
    description     TEXT,
    duration_minutes INT NOT NULL,
    price           DECIMAL(10,2) NOT NULL,
    is_active       BOOLEAN DEFAULT true,
    -- audit columns
    created_at TIMESTAMP NOT NULL DEFAULT now(), created_by UUID,
    updated_at TIMESTAMP, updated_by UUID,
    deleted_at TIMESTAMP, deleted_by UUID, is_deleted BOOLEAN DEFAULT false
);

CREATE INDEX idx_service_shop ON service(shop_id, is_deleted);
CREATE INDEX idx_service_category ON service(category_id);
```

### 3.2 API Endpoints

```
-- Categories
POST   /api/shops/{shopId}/service-categories
GET    /api/shops/{shopId}/service-categories
PUT    /api/shops/{shopId}/service-categories/{id}
DELETE /api/shops/{shopId}/service-categories/{id}

-- Services
POST   /api/shops/{shopId}/services
GET    /api/shops/{shopId}/services          ?categoryId=&active=
GET    /api/shops/{shopId}/services/{id}
PUT    /api/shops/{shopId}/services/{id}
DELETE /api/shops/{shopId}/services/{id}
PUT    /api/shops/{shopId}/services/{id}/toggle-active
```

---

## PHASE 4 — BARBER & STAFF MANAGEMENT
> Priority: CRITICAL | Estimated: 3 days
> Depends on: Phase 3 complete

### 4.1 Flyway Migration

```sql
-- V{N}__create_staff_tables.sql
CREATE TABLE staff (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    shop_id         UUID NOT NULL REFERENCES shop(id),
    user_id         UUID REFERENCES users(id),  -- optional linked user account
    name            VARCHAR(255) NOT NULL,
    photo_url       VARCHAR(500),
    phone           VARCHAR(20),
    email           VARCHAR(255),
    experience_years INT DEFAULT 0,
    bio             TEXT,
    commission_pct  DECIMAL(5,2) DEFAULT 0,
    status          VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    -- audit columns
    created_at TIMESTAMP NOT NULL DEFAULT now(), created_by UUID,
    updated_at TIMESTAMP, updated_by UUID,
    deleted_at TIMESTAMP, deleted_by UUID, is_deleted BOOLEAN DEFAULT false
);

CREATE TABLE staff_service (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    staff_id    UUID NOT NULL REFERENCES staff(id),
    service_id  UUID NOT NULL REFERENCES service(id),
    UNIQUE(staff_id, service_id)
);

CREATE TABLE staff_availability (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    staff_id        UUID NOT NULL REFERENCES staff(id),
    day_of_week     VARCHAR(10) NOT NULL,
    shift_start     TIME,
    shift_end       TIME,
    break_start     TIME,
    break_end       TIME,
    is_working_day  BOOLEAN DEFAULT true
);

CREATE INDEX idx_staff_shop ON staff(shop_id, is_deleted);
CREATE INDEX idx_staff_status ON staff(status);
```

### 4.2 Status Enum
`AVAILABLE | BUSY | ON_LEAVE | INACTIVE`

### 4.3 API Endpoints

```
POST   /api/shops/{shopId}/staff
GET    /api/shops/{shopId}/staff             ?status=&serviceId=
GET    /api/shops/{shopId}/staff/{id}
PUT    /api/shops/{shopId}/staff/{id}
DELETE /api/shops/{shopId}/staff/{id}
PUT    /api/shops/{shopId}/staff/{id}/status

POST   /api/shops/{shopId}/staff/{id}/services      → Assign services
DELETE /api/shops/{shopId}/staff/{id}/services/{serviceId}

PUT    /api/shops/{shopId}/staff/{id}/availability  → Full replace availability
GET    /api/shops/{shopId}/staff/{id}/availability
```

---

## PHASE 5 — CUSTOMER MANAGEMENT
> Priority: CRITICAL | Estimated: 2 days
> Depends on: Phase 1 complete

### 5.1 Flyway Migration

```sql
-- V{N}__create_customer_table.sql
CREATE TABLE customer (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    shop_id         UUID NOT NULL REFERENCES shop(id),
    user_id         UUID REFERENCES users(id),
    name            VARCHAR(255) NOT NULL,
    phone           VARCHAR(20),
    email           VARCHAR(255),
    gender          VARCHAR(10),
    date_of_birth   DATE,
    notes           TEXT,
    -- audit columns
    created_at TIMESTAMP NOT NULL DEFAULT now(), created_by UUID,
    updated_at TIMESTAMP, updated_by UUID,
    deleted_at TIMESTAMP, deleted_by UUID, is_deleted BOOLEAN DEFAULT false
);

CREATE INDEX idx_customer_shop ON customer(shop_id, is_deleted);
CREATE INDEX idx_customer_phone ON customer(phone);
CREATE INDEX idx_customer_user ON customer(user_id);
```

### 5.2 API Endpoints

```
POST   /api/shops/{shopId}/customers
GET    /api/shops/{shopId}/customers         ?search=&page=&size=
GET    /api/shops/{shopId}/customers/{id}
PUT    /api/shops/{shopId}/customers/{id}
DELETE /api/shops/{shopId}/customers/{id}
GET    /api/shops/{shopId}/customers/{id}/history   → bookings + payments
```

---

## PHASE 6 — APPOINTMENT BOOKING ENGINE
> Priority: CRITICAL | Estimated: 5 days
> Depends on: Phases 3, 4, 5 complete
> ⚠️ Most complex phase — implement carefully

### 6.1 Flyway Migration

```sql
-- V{N}__create_appointment_table.sql
CREATE TABLE appointment (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    shop_id         UUID NOT NULL REFERENCES shop(id),
    branch_id       UUID REFERENCES branch(id),
    customer_id     UUID NOT NULL REFERENCES customer(id),
    staff_id        UUID NOT NULL REFERENCES staff(id),
    service_id      UUID NOT NULL REFERENCES service(id),
    appointment_date DATE NOT NULL,
    start_time      TIME NOT NULL,
    end_time        TIME NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    notes           TEXT,
    is_walk_in      BOOLEAN DEFAULT false,
    cancelled_at    TIMESTAMP,
    cancellation_reason TEXT,
    checked_in_at   TIMESTAMP,
    completed_at    TIMESTAMP,
    -- audit columns
    created_at TIMESTAMP NOT NULL DEFAULT now(), created_by UUID,
    updated_at TIMESTAMP, updated_by UUID,
    deleted_at TIMESTAMP, deleted_by UUID, is_deleted BOOLEAN DEFAULT false
);

CREATE INDEX idx_appointment_shop_date ON appointment(shop_id, appointment_date, is_deleted);
CREATE INDEX idx_appointment_staff_date ON appointment(staff_id, appointment_date);
CREATE INDEX idx_appointment_customer ON appointment(customer_id);
CREATE INDEX idx_appointment_status ON appointment(status);
```

### 6.2 Status Enum
`PENDING | CONFIRMED | CHECKED_IN | IN_PROGRESS | COMPLETED | CANCELLED | NO_SHOW`

### 6.3 API Endpoints

```
POST   /api/shops/{shopId}/appointments           → Create (owner/staff)
POST   /api/shops/{shopId}/appointments/walk-in   → Walk-in booking
GET    /api/shops/{shopId}/appointments           ?date=&staffId=&status=&page=
GET    /api/shops/{shopId}/appointments/{id}
PUT    /api/shops/{shopId}/appointments/{id}      → Reschedule
PATCH  /api/shops/{shopId}/appointments/{id}/status → Status transition
DELETE /api/shops/{shopId}/appointments/{id}      → Cancel

-- Customer-facing
POST   /api/public/shops/{shopId}/bookings        → Customer self-booking
GET    /api/customers/appointments                → Customer's own appointments
```

### 6.4 Status Transition Rules (enforce in service layer)
```
PENDING    → CONFIRMED | CANCELLED
CONFIRMED  → CHECKED_IN | CANCELLED | NO_SHOW
CHECKED_IN → IN_PROGRESS
IN_PROGRESS → COMPLETED
COMPLETED  → (terminal)
CANCELLED  → (terminal)
NO_SHOW   → (terminal)
```

### 6.5 Booking Flow Validation (BookingService)
```
1. Validate shop exists and is APPROVED
2. Validate service belongs to shop
3. Validate staff belongs to shop and offers the service
4. Validate appointment_date is not in the past
5. Validate time slot is within staff working hours
6. Validate no existing appointment conflicts for staff at that time
7. Calculate end_time = start_time + service.durationMinutes
8. Save appointment
9. Publish AppointmentCreatedEvent (for notifications)
```

---

## PHASE 7 — AVAILABILITY & SLOT ENGINE
> Priority: CRITICAL | Estimated: 3 days
> Depends on: Phase 6 complete
> Purpose: Generate available time slots, prevent double booking

### 7.1 SlotEngine Service Logic

```java
// Input: shopId, staffId, serviceId, date
// Output: List<TimeSlot> (available slots only)

// Algorithm:
// 1. Get staff availability for that dayOfWeek
// 2. Get service duration in minutes
// 3. Generate all possible slots: shift_start to shift_end, step = serviceDuration
// 4. Remove slots that overlap with break time
// 5. Remove slots that conflict with existing PENDING/CONFIRMED/CHECKED_IN/IN_PROGRESS appointments
// 6. Remove slots in the past (if date == today)
// 7. Return remaining slots
```

### 7.2 API Endpoints

```
GET /api/shops/{shopId}/slots
    ?staffId={uuid}
    &serviceId={uuid}
    &date={yyyy-MM-dd}
→ List<{ startTime, endTime, available: true }>
```

### 7.3 Leave Integration (Phase 11 preview)
```
// If staff has approved leave on that date → return empty slots
// Check leave table if it exists; skip if not yet implemented
```

---

## PHASE 8 — NOTIFICATION SYSTEM
> Priority: HIGH | Estimated: 2-3 days
> Depends on: Phase 6 complete

### 8.1 Architecture
```
Spring ApplicationEvent → NotificationEventListener → NotificationService
NotificationService → EmailChannel | SMSChannel | WhatsAppChannel
Use @Async for all notification sending
```

### 8.2 Flyway Migration

```sql
-- V{N}__create_notification_table.sql
CREATE TABLE notification_log (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    shop_id     UUID REFERENCES shop(id),
    recipient   VARCHAR(255) NOT NULL,
    channel     VARCHAR(20) NOT NULL,   -- EMAIL, SMS, WHATSAPP
    event_type  VARCHAR(50) NOT NULL,
    subject     VARCHAR(500),
    body        TEXT,
    status      VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    sent_at     TIMESTAMP,
    error_msg   TEXT,
    created_at  TIMESTAMP NOT NULL DEFAULT now()
);
```

### 8.3 Events to Handle
```
BOOKING_CREATED   → Notify customer (confirmation) + staff
BOOKING_CONFIRMED → Notify customer
BOOKING_REMINDER  → 1 hour before (scheduled via @Scheduled)
BOOKING_CANCELLED → Notify customer + staff
BOOKING_COMPLETED → Notify customer (ask for review)
PAYMENT_RECEIVED  → Notify customer (receipt)
```

### 8.4 Email Implementation
```java
// Use Spring Mail (JavaMailSender)
// Use Thymeleaf for HTML email templates
// Templates in: resources/templates/email/
// Config: spring.mail.* in application.yml
```

### 8.5 SMS/WhatsApp (stub for now)
```java
// Create interface: SmsGateway with send(phone, message)
// Create stub implementation that just logs
// Real implementation (Twilio/MSG91) added later
```

---

## PHASE 9 — PAYMENT MODULE
> Priority: HIGH | Estimated: 2-3 days
> Depends on: Phase 6 complete

### 9.1 Flyway Migration

```sql
-- V{N}__create_payment_table.sql
CREATE TABLE payment (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    shop_id             UUID NOT NULL REFERENCES shop(id),
    appointment_id      UUID REFERENCES appointment(id),
    customer_id         UUID NOT NULL REFERENCES customer(id),
    amount              DECIMAL(10,2) NOT NULL,
    discount_amount     DECIMAL(10,2) DEFAULT 0,
    final_amount        DECIMAL(10,2) NOT NULL,
    payment_method      VARCHAR(20) NOT NULL,  -- CASH, UPI, CARD, ONLINE
    payment_status      VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    transaction_ref     VARCHAR(255),
    gateway_ref         VARCHAR(255),
    paid_at             TIMESTAMP,
    notes               TEXT,
    -- audit columns
    created_at TIMESTAMP NOT NULL DEFAULT now(), created_by UUID,
    updated_at TIMESTAMP, updated_by UUID,
    is_deleted BOOLEAN DEFAULT false
);

CREATE INDEX idx_payment_shop ON payment(shop_id);
CREATE INDEX idx_payment_appointment ON payment(appointment_id);
CREATE INDEX idx_payment_customer ON payment(customer_id);
```

### 9.2 Payment Method Enum
`CASH | UPI | CARD | ONLINE`

### 9.3 Status Enum
`PENDING | PAID | FAILED | REFUNDED`

### 9.4 API Endpoints

```
POST   /api/shops/{shopId}/payments              → Record payment
GET    /api/shops/{shopId}/payments              ?date=&status=&method=&page=
GET    /api/shops/{shopId}/payments/{id}
PATCH  /api/shops/{shopId}/payments/{id}/refund  → Mark refunded

GET    /api/shops/{shopId}/appointments/{id}/payment  → Get payment for appointment
```

---

## PHASE 19 — ACTIVITY LOGS & AUDIT
> Priority: CRITICAL | Implement alongside every other phase

### 19.1 Flyway Migration

```sql
-- V{N}__create_activity_log_table.sql
CREATE TABLE activity_log (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    shop_id     UUID REFERENCES shop(id),
    user_id     UUID REFERENCES users(id),
    module      VARCHAR(50) NOT NULL,
    action      VARCHAR(50) NOT NULL,
    entity_id   UUID,
    entity_type VARCHAR(100),
    old_value   JSONB,
    new_value   JSONB,
    ip_address  VARCHAR(50),
    user_agent  VARCHAR(500),
    created_at  TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_activity_shop ON activity_log(shop_id, created_at DESC);
CREATE INDEX idx_activity_user ON activity_log(user_id, created_at DESC);
CREATE INDEX idx_activity_module ON activity_log(module, action);
```

### 19.2 Implementation Pattern

```java
// Use AOP — ActivityLogAspect.java
// Annotate service methods with @Auditable(module="SHOP", action="UPDATE")
// Aspect captures: old value (before), new value (after), user from SecurityContext
// Save to activity_log table asynchronously

@Auditable(module = "SHOP", action = "CREATE")
public ShopResponse createShop(CreateShopRequest request) { ... }
```

### 19.3 API Endpoints

```
GET /api/admin/activity-logs          ?shopId=&userId=&module=&from=&to=&page=
GET /api/shops/{shopId}/activity-logs ?module=&from=&to=&page=
```

---

## PHASE 20 — PLATFORM ADMINISTRATION
> Priority: CRITICAL | Estimated: 3 days
> Depends on: All previous phases

### 20.1 Admin API Endpoints

```
-- Shop Management
GET    /api/admin/shops               ?status=&city=&page=
GET    /api/admin/shops/{id}
PUT    /api/admin/shops/{id}/verify   { status: APPROVED|REJECTED, reason: string }
PUT    /api/admin/shops/{id}/suspend
PUT    /api/admin/shops/{id}/reactivate

-- Platform Analytics
GET    /api/admin/analytics/summary   → total shops, active users, revenue, bookings today
GET    /api/admin/analytics/revenue   ?from=&to=&groupBy=day|week|month
GET    /api/admin/analytics/shops     → top shops by bookings/revenue

-- User Management
GET    /api/admin/users               ?role=&page=
PUT    /api/admin/users/{id}/suspend
```

### 20.2 Frontend Admin Pages

```
/admin                     → Admin dashboard
/admin/shops               → Shop list with verify actions
/admin/shops/{id}          → Shop detail
/admin/users               → User management
/admin/analytics           → Revenue + booking charts
/admin/activity-logs       → Audit trail
```

---

## REMAINING PHASES — IMPLEMENT AFTER MVP

```
Phase 10 — Reviews & Ratings
Phase 11 — Attendance & Leave
Phase 12 — Loyalty & Rewards
Phase 13 — Coupons & Promotions
Phase 14 — Subscription & Billing   ← Revenue critical
Phase 15 — Analytics & Reporting
Phase 16 — Inventory Management
Phase 17 — POS Billing
Phase 18 — Public Marketplace
```

### Phase 14 Quick Reference (Subscription)

```sql
-- subscription_plan: id, name, price_monthly, price_annual, max_branches,
--                    max_staff, max_appointments_per_month, features JSONB
-- shop_subscription: id, shop_id, plan_id, start_date, end_date, status, billing_cycle
```

---

## MVP CHECKLIST
> The product is sellable when ALL of these are done

```
[ ] Phase 0  — Auth & RBAC verified
[ ] Phase 1  — Shop CRUD + Hours + Location + Verification
[ ] Phase 2  — Branch Management
[ ] Phase 3  — Service Categories + Services
[ ] Phase 4  — Staff + Services + Availability
[ ] Phase 5  — Customer profiles + history
[ ] Phase 6  — Appointment booking + status machine
[ ] Phase 7  — Slot availability engine
[ ] Phase 8  — Email notifications
[ ] Phase 9  — Payment recording
[ ] Phase 19 — Activity logging on all modules
[ ] Phase 20 — Admin panel for shop verification
```

---

## ERROR PREVENTION RULES (Read before every implementation)

```
1. NEVER hard delete — always soft delete with is_deleted + deleted_at + deleted_by
2. NEVER query without filtering is_deleted = false
3. NEVER skip shop_id tenant isolation on any business query
4. NEVER allow cross-tenant data access — validate shopId ownership in every endpoint
5. NEVER implement payments without wrapping in @Transactional
6. NEVER generate slots without checking existing appointments (race condition risk)
7. NEVER send notifications synchronously — always @Async
8. ALWAYS validate status transitions in the service layer
9. ALWAYS return paginated responses for list endpoints
10. ALWAYS use DTOs — never expose entities directly in API responses
11. ALWAYS add Flyway migrations — never use ddl-auto: create or update
12. ALWAYS check shop ownership before allowing mutation
```

---

## QUICK COMMAND REFERENCE

```bash
# Run migrations only
./mvnw flyway:migrate

# Run backend
./mvnw spring-boot:run

# Run frontend
cd frontend && npm run dev

# Docker full stack
docker-compose up -d

# Run tests
./mvnw test

# Check migration status
./mvnw flyway:info
```

---

## CONTEXT LOADING GUIDE FOR AI IDE

When working on a specific phase, load only:
```
1. This file (BARBER_SALON_SAAS_PRD.md) — always
2. The specific phase section you're implementing
3. The existing BaseEntity.java
4. The existing SecurityConfig.java
5. The existing ApiResponse.java
6. 1-2 existing similar entities/controllers as reference
7. Current Flyway migration files (to get next version number)
```

Do NOT load all source files at once — it wastes tokens and causes confusion.
Work module by module: Entity → Migration → Repository → Service → Controller → DTO → Mapper → Frontend

---

*Document Version: 1.0 | Project: Barber & Salon SaaS | Stack: Java 21 + Spring Boot 3 + React 19*
