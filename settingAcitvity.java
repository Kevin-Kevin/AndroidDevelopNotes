protected void onCreate(Bundle savedState) {
        // Should happen before any call to getIntent()
        getMetaData();
        final Intent intent = getIntent();

        if (shouldShowTwoPaneDeepLink(intent)) {
            launchHomepageForTwoPaneDeepLink(intent);
            finishAndRemoveTask();
            super.onCreate(savedState);
            return;
        }

        super.onCreate(savedState);
        Log.d(LOG_TAG, "Starting onCreate");

        long startTime = System.currentTimeMillis();

        final FeatureFactory factory = FeatureFactory.getFactory(this);
        mDashboardFeatureProvider = factory.getDashboardFeatureProvider(this);

        if (intent.hasExtra(EXTRA_UI_OPTIONS)) {
            getWindow().setUiOptions(intent.getIntExtra(EXTRA_UI_OPTIONS, 0));
        }

        // Getting Intent properties can only be done after the super.onCreate(...)
        final String initialFragmentName = getInitialFragmentName(intent);

        // If this is a sub settings, then apply the SubSettings Theme for the ActionBar content
        // insets.
        // If this is in setup flow, don't apply theme. Because light theme needs to be applied
        // in SettingsBaseActivity#onCreate().
        if (isSubSettings(intent) && !WizardManagerHelper.isAnySetupWizard(getIntent())) {
            setTheme(R.style.Theme_SubSettings);
        }

        setContentView(R.layout.settings_main_prefs);

        getSupportFragmentManager().addOnBackStackChangedListener(this);

        if (savedState != null) {
            // We are restarting from a previous saved state; used that to initialize, instead
            // of starting fresh.
            setTitleFromIntent(intent);

            ArrayList<DashboardCategory> categories =
                    savedState.getParcelableArrayList(SAVE_KEY_CATEGORIES);
            if (categories != null) {
                mCategories.clear();
                mCategories.addAll(categories);
                setTitleFromBackStack();
            }
        } else {
            launchSettingFragment(initialFragmentName, intent);
        }

        final boolean isInSetupWizard = WizardManagerHelper.isAnySetupWizard(getIntent());

        final ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(!isInSetupWizard);
            actionBar.setHomeButtonEnabled(!isInSetupWizard);
            actionBar.setDisplayShowTitleEnabled(true);
        }
        mMainSwitch = findViewById(R.id.switch_bar);
        if (mMainSwitch != null) {
            mMainSwitch.setMetricsTag(getMetricsTag());
            mMainSwitch.setTranslationZ(findViewById(R.id.main_content).getTranslationZ() + 1);
        }

        // see if we should show Back/Next buttons
        if (intent.getBooleanExtra(EXTRA_PREFS_SHOW_BUTTON_BAR, false)) {

            View buttonBar = findViewById(R.id.button_bar);
            if (buttonBar != null) {
                buttonBar.setVisibility(View.VISIBLE);

                Button backButton = findViewById(R.id.back_button);
                backButton.setOnClickListener(v -> {
                    setResult(RESULT_CANCELED, null);
                    finish();
                });
                Button skipButton = findViewById(R.id.skip_button);
                skipButton.setOnClickListener(v -> {
                    setResult(RESULT_OK, null);
                    finish();
                });
                mNextButton = findViewById(R.id.next_button);
                mNextButton.setOnClickListener(v -> {
                    setResult(RESULT_OK, null);
                    finish();
                });

                // set our various button parameters
                if (intent.hasExtra(EXTRA_PREFS_SET_NEXT_TEXT)) {
                    String buttonText = intent.getStringExtra(EXTRA_PREFS_SET_NEXT_TEXT);
                    if (TextUtils.isEmpty(buttonText)) {
                        mNextButton.setVisibility(View.GONE);
                    } else {
                        mNextButton.setText(buttonText);
                    }
                }
                if (intent.hasExtra(EXTRA_PREFS_SET_BACK_TEXT)) {
                    String buttonText = intent.getStringExtra(EXTRA_PREFS_SET_BACK_TEXT);
                    if (TextUtils.isEmpty(buttonText)) {
                        backButton.setVisibility(View.GONE);
                    } else {
                        backButton.setText(buttonText);
                    }
                }
                if (intent.getBooleanExtra(EXTRA_PREFS_SHOW_SKIP, false)) {
                    skipButton.setVisibility(View.VISIBLE);
                }
            }
        }

        if (DEBUG_TIMING) {
            Log.d(LOG_TAG, "onCreate took " + (System.currentTimeMillis() - startTime) + " ms");
        }
    }