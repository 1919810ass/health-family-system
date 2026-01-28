I have implemented the "Seasonal Wellness" feature as requested, covering both backend and frontend.

### Backend Implementation
1.  **SolarTermUtil**: Created `com.healthfamily.utils.SolarTermUtil` with a simplified astronomical algorithm (lookup table for the 21st century) to calculate the 24 solar terms without external APIs.
2.  **DTO**: Created `SeasonalWellnessDTO` to transfer data.
3.  **Service**:
    *   Created `SeasonalWellnessService` interface and `SeasonalWellnessServiceImpl`.
    *   Implemented logic to fetch the current solar term and the user's latest `ConstitutionAssessment`.
    *   Integrated **Spring AI `ChatClient`** to generate personalized advice based on the term and constitution.
    *   Added **Redis Caching** (24h expiry) to optimize token usage.
4.  **Controller**: Created `SeasonalWellnessController` exposing `GET /api/wellness/today`.

### Frontend Implementation
1.  **Component**: Created `FrontEnd/src/views/Home/components/SeasonalWellnessCard.vue`.
    *   Displays the solar term with a Chinese-style aesthetic.
    *   Shows personalized advice with a "Loading" state.
    *   Falls back to a gradient background if no specific image is provided.
2.  **Integration**: Updated `FrontEnd/src/views/Home/index.vue` to include the `SeasonalWellnessCard` at the top of the dashboard.

### Verification
- **Code Structure**: Followed the existing Controller-Service-Repository pattern.
- **Dependencies**: Used existing `ConstitutionAssessmentRepository` and `UserPrincipal`.
- **UI**: Integrated seamlessly into the Home view using Element Plus and Scoped SCSS.

The feature is ready to run. Ensure your Redis service is active and Spring AI API keys are configured in `application.yml`.