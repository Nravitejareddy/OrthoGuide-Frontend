import os

base_dir = r"c:\Users\AD-LAB\AndroidStudioProjects\OrthoGuide\app\src\main\res"
layout_file = os.path.join(base_dir, "layout", "activity_dashboard.xml")
header_file = os.path.join(base_dir, "drawable", "bg_dashboard_header.xml")

original_step_185 = """<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/surface_light"
    tools:context=".DashboardActivity">

    <!-- Scrollable Content -->
    <androidx.core.widget.NestedScrollView
        android:id="@+id/nsv_content"
        android:layout_width="0dp"
        android:layout_height="0dp"
        android:clipToPadding="false"
        android:fillViewport="true"
        android:paddingBottom="48dp"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintBottom_toTopOf="@id/bottom_nav"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent">

        <androidx.constraintlayout.widget.ConstraintLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content">

            <!-- Green Header -->
            <View
                android:id="@+id/dashboard_header_bg"
                android:layout_width="0dp"
                android:layout_height="280dp"
                android:background="@drawable/bg_dashboard_header"
                app:layout_constraintTop_toTopOf="parent"
                app:layout_constraintStart_toStartOf="parent"
                app:layout_constraintEnd_toEndOf="parent" />

            <!-- Top Bar -->
            <LinearLayout
                android:id="@+id/ll_top_bar"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:orientation="horizontal"
                android:padding="24dp"
                android:gravity="center_vertical"
                app:layout_constraintTop_toTopOf="parent"
                app:layout_constraintStart_toStartOf="parent"
                app:layout_constraintEnd_toEndOf="parent">

                <ImageView
                    android:layout_width="36dp"
                    android:layout_height="36dp"
                    android:src="@drawable/ic_tooth_splash" />

                <TextView
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_weight="1"
                    android:layout_marginStart="12dp"
                    android:text="@string/dashboard_app_name"
                    android:textColor="@color/white"
                    android:textSize="20sp"
                    android:textStyle="bold" />

                <FrameLayout
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:background="@drawable/bg_pill_status"
                    android:paddingHorizontal="4dp">
                    
                    <ImageView
                        android:id="@+id/iv_notification_bell"
                        android:layout_width="40dp"
                        android:layout_height="40dp"
                        android:padding="8dp"
                        android:src="@drawable/ic_notification_bell"
                        android:scaleType="centerInside" />

                    <View
                        android:layout_width="8dp"
                        android:layout_height="8dp"
                        android:layout_gravity="top|end"
                        android:layout_marginTop="8dp"
                        android:layout_marginEnd="8dp"
                        android:background="@drawable/bg_circle_yellow" />
                </FrameLayout>
            </LinearLayout>

            <!-- Greeting & User Info -->
            <LinearLayout
                android:id="@+id/ll_user_info"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:paddingHorizontal="24dp"
                app:layout_constraintTop_toBottomOf="@id/ll_top_bar"
                app:layout_constraintStart_toStartOf="parent"
                app:layout_constraintEnd_toEndOf="parent">

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="@string/dashboard_greeting"
                    android:textColor="@color/white"
                    android:alpha="0.85"
                    android:textSize="16sp"
                    android:letterSpacing="0.02" />

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="@string/dashboard_user_name"
                    android:textColor="@color/white"
                    android:textSize="28sp"
                    android:textStyle="bold"
                    android:layout_marginTop="4dp" />

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="12dp"
                    android:background="@drawable/bg_pill_status"
                    android:paddingHorizontal="16dp"
                    android:paddingVertical="6dp"
                    android:text="@string/treatment_in_progress"
                    android:textColor="@color/white"
                    android:textSize="14sp" />
            </LinearLayout>

            <!-- Dynamic Content Container -->
            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:padding="24dp"
                app:layout_constraintTop_toBottomOf="@id/ll_user_info"
                app:layout_constraintStart_toStartOf="parent"
                app:layout_constraintEnd_toEndOf="parent">

                <!-- Progress Card -->
                <com.google.android.material.card.MaterialCardView
                    android:id="@+id/card_progress"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="24dp"
                    app:cardCornerRadius="28dp"
                    app:cardElevation="8dp"
                    app:cardBackgroundColor="@color/surface_card"
                    app:strokeWidth="0dp">

                    <LinearLayout
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:orientation="vertical"
                        android:gravity="center"
                        android:paddingTop="32dp"
                        android:paddingBottom="24dp"
                        android:paddingHorizontal="24dp">

                        <FrameLayout
                            android:layout_width="140dp"
                            android:layout_height="140dp">

                            <ProgressBar
                                android:layout_width="match_parent"
                                android:layout_height="match_parent"
                                android:background="@drawable/bg_progress_circle"
                                android:indeterminate="false"
                                android:progress="33"
                                android:progressDrawable="@drawable/bg_progress_circle"
                                style="?android:attr/progressBarStyleHorizontal" />

                            <LinearLayout
                                android:layout_width="wrap_content"
                                android:layout_height="wrap_content"
                                android:layout_gravity="center"
                                android:orientation="vertical"
                                android:gravity="center">

                                <TextView
                                    android:layout_width="wrap_content"
                                    android:layout_height="wrap_content"
                                    android:text="@string/treatment_complete_pct"
                                    android:textColor="#059669"
                                    android:textSize="32sp"
                                    android:textStyle="bold" />

                                <TextView
                                    android:layout_width="wrap_content"
                                    android:layout_height="wrap_content"
                                    android:text="@string/complete_label"
                                    android:textColor="#9CA3AF"
                                    android:textSize="12sp"
                                    android:textStyle="bold" />
                            </LinearLayout>
                        </FrameLayout>

                        <TextView
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:layout_marginTop="24dp"
                            android:text="@string/current_stage_label"
                            android:textColor="#1F2937"
                            android:textSize="18sp"
                            android:textStyle="bold" />

                        <TextView
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:layout_marginTop="4dp"
                            android:text="@string/current_stage_value"
                            android:textColor="#6B7280"
                            android:textSize="14sp" />

                    </LinearLayout>
                </com.google.android.material.card.MaterialCardView>

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="32dp"
                    android:text="@string/next_appointment_label"
                    android:textColor="#9CA3AF"
                    android:textSize="12sp"
                    android:textStyle="bold"
                    android:letterSpacing="0.05" />

                <!-- Appointment Card -->
                <com.google.android.material.card.MaterialCardView
                    android:id="@+id/card_appointment"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="12dp"
                    app:cardCornerRadius="20dp"
                    app:cardElevation="2dp"
                    app:cardBackgroundColor="@color/surface_card"
                    app:strokeWidth="1dp"
                    app:strokeColor="@color/border_light">

                    <LinearLayout
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:orientation="horizontal"
                        android:padding="16dp"
                        android:gravity="center_vertical">

                        <FrameLayout
                            android:layout_width="56dp"
                            android:layout_height="56dp">
                            <View
                                android:layout_width="match_parent"
                                android:layout_height="match_parent"
                                android:background="@drawable/bg_calendar_date" />
                            <LinearLayout
                                android:layout_width="wrap_content"
                                android:layout_height="wrap_content"
                                android:layout_gravity="center"
                                android:orientation="vertical"
                                android:gravity="center">
                                <TextView
                                    android:layout_width="wrap_content"
                                    android:layout_height="wrap_content"
                                    android:text="OCT"
                                    android:textColor="@color/white"
                                    android:textSize="10sp"
                                    android:textStyle="bold" />
                                <TextView
                                    android:layout_width="wrap_content"
                                    android:layout_height="wrap_content"
                                    android:text="24"
                                    android:textColor="@color/white"
                                    android:textSize="20sp"
                                    android:textStyle="bold" />
                            </LinearLayout>
                        </FrameLayout>

                        <LinearLayout
                            android:layout_width="0dp"
                            android:layout_height="wrap_content"
                            android:layout_weight="1"
                            android:layout_marginStart="16dp"
                            android:orientation="vertical">

                            <TextView
                                android:layout_width="wrap_content"
                                android:layout_height="wrap_content"
                                android:text="@string/appointment_title"
                                android:textColor="@color/text_title"
                                android:textSize="17sp"
                                android:textStyle="bold"
                                android:letterSpacing="-0.01" />

                            <TextView
                                android:layout_width="wrap_content"
                                android:layout_height="wrap_content"
                                android:layout_marginTop="2dp"
                                android:text="@string/appointment_details"
                                android:textColor="@color/text_body"
                                android:textSize="14sp"
                                android:lineSpacingExtra="2dp" />
                        </LinearLayout>

                        <ImageView
                            android:layout_width="24dp"
                            android:layout_height="24dp"
                            android:src="@drawable/ic_chevron_right"
                            app:tint="#D1D5DB" />
                    </LinearLayout>
                </com.google.android.material.card.MaterialCardView>

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="32dp"
                    android:text="@string/quick_actions_label"
                    android:textColor="#9CA3AF"
                    android:textSize="12sp"
                    android:textStyle="bold"
                    android:letterSpacing="0.05" />

                <!-- Quick Actions Grid -->
                <GridLayout
                    android:id="@+id/grid_actions"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="12dp"
                    android:columnCount="2"
                    android:rowCount="2"
                    android:useDefaultMargins="false">

                    <!-- Action 1: AI Assistant -->
                    <com.google.android.material.card.MaterialCardView
                        android:id="@+id/card_ai_assistant"
                        android:layout_width="0dp"
                        android:layout_height="wrap_content"
                        android:layout_columnWeight="1"
                        android:layout_marginEnd="8dp"
                        android:layout_marginBottom="16dp"
                        app:cardCornerRadius="20dp"
                        app:cardElevation="2dp"
                        app:cardBackgroundColor="@color/surface_card"
                        app:strokeWidth="1dp"
                        app:strokeColor="@color/border_light">
                        <LinearLayout
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:orientation="vertical"
                            android:gravity="center"
                            android:padding="20dp">
                            <ImageView
                                android:layout_width="48dp"
                                android:layout_height="48dp"
                                android:padding="12dp"
                                android:src="@drawable/ic_action_ai"
                                android:background="@drawable/bg_action_ai"
                                app:tint="@color/white" />
                            <TextView
                                android:layout_width="wrap_content"
                                android:layout_height="wrap_content"
                                android:layout_marginTop="12dp"
                                android:text="@string/action_ai_assistant"
                                android:textColor="@color/text_title"
                                android:textSize="14sp"
                                android:textStyle="bold"
                                android:letterSpacing="0.01" />
                        </LinearLayout>
                    </com.google.android.material.card.MaterialCardView>

                    <!-- Action 2: Reminders -->
                    <com.google.android.material.card.MaterialCardView
                        android:id="@+id/card_reminders"
                        android:layout_width="0dp"
                        android:layout_height="wrap_content"
                        android:layout_columnWeight="1"
                        android:layout_marginStart="8dp"
                        android:layout_marginBottom="16dp"
                        app:cardCornerRadius="20dp"
                        app:cardElevation="2dp"
                        app:cardBackgroundColor="@color/surface_card"
                        app:strokeWidth="1dp"
                        app:strokeColor="@color/border_light">
                        <LinearLayout
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:orientation="vertical"
                            android:gravity="center"
                            android:padding="20dp">
                            <ImageView
                                android:layout_width="48dp"
                                android:layout_height="48dp"
                                android:padding="12dp"
                                android:src="@drawable/ic_action_reminders"
                                android:background="@drawable/bg_action_reminders"
                                app:tint="@color/white" />
                            <TextView
                                android:layout_width="wrap_content"
                                android:layout_height="wrap_content"
                                android:layout_marginTop="12dp"
                                android:text="@string/action_reminders"
                                android:textColor="@color/text_title"
                                android:textSize="14sp"
                                android:textStyle="bold"
                                android:letterSpacing="0.01" />
                        </LinearLayout>
                    </com.google.android.material.card.MaterialCardView>

                    <!-- Action 3: Care Guide -->
                    <com.google.android.material.card.MaterialCardView
                        android:id="@+id/card_care_guide"
                        android:layout_width="0dp"
                        android:layout_height="wrap_content"
                        android:layout_columnWeight="1"
                        android:layout_marginEnd="8dp"
                        android:layout_marginBottom="16dp"
                        app:cardCornerRadius="20dp"
                        app:cardElevation="2dp"
                        app:cardBackgroundColor="@color/surface_card"
                        app:strokeWidth="1dp"
                        app:strokeColor="@color/border_light">
                        <LinearLayout
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:orientation="vertical"
                            android:gravity="center"
                            android:padding="20dp">
                            <ImageView
                                android:layout_width="48dp"
                                android:layout_height="48dp"
                                android:padding="12dp"
                                android:src="@drawable/ic_action_care"
                                android:background="@drawable/bg_action_care"
                                app:tint="@color/white" />
                            <TextView
                                android:layout_width="wrap_content"
                                android:layout_height="wrap_content"
                                android:layout_marginTop="12dp"
                                android:text="@string/action_care_guide"
                                android:textColor="@color/text_title"
                                android:textSize="14sp"
                                android:textStyle="bold"
                                android:letterSpacing="0.01" />
                        </LinearLayout>
                    </com.google.android.material.card.MaterialCardView>

                    <!-- Action 4: Report Issue -->
                    <com.google.android.material.card.MaterialCardView
                        android:id="@+id/card_report_issue"
                        android:layout_width="0dp"
                        android:layout_height="wrap_content"
                        android:layout_columnWeight="1"
                        android:layout_marginStart="8dp"
                        android:layout_marginBottom="16dp"
                        app:cardCornerRadius="20dp"
                        app:cardElevation="2dp"
                        app:cardBackgroundColor="@color/surface_card"
                        app:strokeWidth="1dp"
                        app:strokeColor="@color/border_light">
                        <LinearLayout
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:orientation="vertical"
                            android:gravity="center"
                            android:padding="20dp">
                            <ImageView
                                android:layout_width="48dp"
                                android:layout_height="48dp"
                                android:padding="12dp"
                                android:src="@drawable/ic_action_report"
                                android:background="@drawable/bg_action_report"
                                app:tint="@color/white" />
                            <TextView
                                android:layout_width="wrap_content"
                                android:layout_height="wrap_content"
                                android:layout_marginTop="12dp"
                                android:text="@string/action_report_issue"
                                android:textColor="@color/text_title"
                                android:textSize="14sp"
                                android:textStyle="bold"
                                android:letterSpacing="0.01" />
                        </LinearLayout>
                    </com.google.android.material.card.MaterialCardView>

                </GridLayout>

            </LinearLayout>
        </androidx.constraintlayout.widget.ConstraintLayout>
    </androidx.core.widget.NestedScrollView>

    <!-- Traditional Bottom Nav Bar -->
    <LinearLayout
        android:id="@+id/bottom_nav"
        android:layout_width="0dp"
        android:layout_height="72dp"
        android:background="@color/white"
        android:orientation="horizontal"
        android:gravity="center_vertical"
        android:elevation="8dp"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent">

        <LinearLayout style="@style/BottomNavTab" android:id="@+id/tab_home">
            <ImageView style="@style/BottomNavIcon" android:src="@drawable/ic_home_nav" app:tint="#10B981" />
            <TextView style="@style/BottomNavText" android:text="@string/nav_home" android:textColor="#10B981" />
        </LinearLayout>

        <LinearLayout style="@style/BottomNavTab" android:id="@+id/tab_chat">
            <ImageView style="@style/BottomNavIcon" android:src="@drawable/ic_chat_nav" />
            <TextView style="@style/BottomNavText" android:text="@string/nav_chat" />
        </LinearLayout>

        <LinearLayout style="@style/BottomNavTab" android:id="@+id/tab_progress">
            <ImageView style="@style/BottomNavIcon" android:src="@drawable/ic_progress_nav" />
            <TextView style="@style/BottomNavText" android:text="@string/nav_progress" />
        </LinearLayout>

        <LinearLayout style="@style/BottomNavTab" android:id="@+id/tab_profile">
            <!-- Active State -->
            <ImageView style="@style/BottomNavIcon" android:src="@drawable/ic_profile_nav" />
            <TextView style="@style/BottomNavText" android:text="@string/nav_profile" />
        </LinearLayout>

    </LinearLayout>

</androidx.constraintlayout.widget.ConstraintLayout>
"""

header_content = """<?xml version="1.0" encoding="utf-8"?>
<layer-list xmlns:android="http://schemas.android.com/apk/res/android">
    <!-- Base Gradient -->
    <item>
        <shape android:shape="rectangle">
            <gradient
                android:angle="270"
                android:endColor="#059669"
                android:startColor="#10B981" />
            <corners
                android:bottomLeftRadius="36dp"
                android:bottomRightRadius="36dp" />
        </shape>
    </item>
    
    <!-- Subtle Top Sheen -->
    <item android:bottom="140dp">
        <shape android:shape="rectangle">
            <gradient
                android:angle="270"
                android:startColor="#1AFFFFFF"
                android:endColor="#00FFFFFF" />
        </shape>
    </item>
</layer-list>
"""

with open(layout_file, 'w', encoding='utf-8') as f:
    f.write(original_step_185)

with open(header_file, 'w', encoding='utf-8') as f:
    f.write(header_content)
