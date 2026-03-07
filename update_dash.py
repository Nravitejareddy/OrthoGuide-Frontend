import re
import os

def rewrite():
    base_dir = r"c:\Users\AD-LAB\AndroidStudioProjects\OrthoGuide\app\src\main\res"
    layout_file = os.path.join(base_dir, "layout", "activity_dashboard.xml")
    
    with open(layout_file, 'r', encoding='utf-8') as f:
        content = f.read()
        
    bottom_nav_idx = content.find("<!-- Traditional Bottom Nav Bar -->")
    bottom_nav_content = content[bottom_nav_idx:]

    header_gradient = """<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android">
    <gradient
        android:angle="225"
        android:endColor="#0DAA74"
        android:startColor="#17C98C" />
    <corners
        android:bottomLeftRadius="48dp"
        android:bottomRightRadius="48dp" />
</shape>"""
    with open(os.path.join(base_dir, "drawable", "bg_dashboard_header.xml"), 'w', encoding='utf-8') as f:
        f.write(header_gradient)

    new_content = """<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#F9FAFB"
    tools:context=".DashboardActivity">

    <androidx.core.widget.NestedScrollView
        android:id="@+id/nsv_content"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:fillViewport="true"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintBottom_toTopOf="@id/bottom_nav"
        android:overScrollMode="never">

        <androidx.constraintlayout.widget.ConstraintLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:paddingBottom="48dp">

            <!-- Green Header Background -->
            <View
                android:id="@+id/dashboard_header_bg"
                android:layout_width="match_parent"
                android:layout_height="320dp"
                android:background="@drawable/bg_dashboard_header"
                app:layout_constraintTop_toTopOf="parent" />

            <!-- Top Bar -->
            <LinearLayout
                android:id="@+id/ll_top_bar"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="horizontal"
                android:paddingHorizontal="24dp"
                android:paddingTop="48dp"
                android:gravity="center_vertical"
                app:layout_constraintTop_toTopOf="parent">

                <ImageView
                    android:layout_width="28dp"
                    android:layout_height="28dp"
                    android:src="@drawable/ic_tooth_splash"
                    app:tint="@color/white" />

                <TextView
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_weight="1"
                    android:layout_marginStart="8dp"
                    android:text="OrthoGuide"
                    android:textColor="@color/white"
                    android:textSize="20sp" />

                <FrameLayout
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:background="@drawable/bg_pill_status"
                    android:paddingHorizontal="4dp"
                    android:paddingVertical="4dp">
                    
                    <ImageView
                        android:id="@+id/iv_notification_bell"
                        android:layout_width="32dp"
                        android:layout_height="32dp"
                        android:padding="6dp"
                        android:layout_gravity="center"
                        android:scaleType="centerInside"
                        android:src="@drawable/ic_notification_bell"
                        app:tint="#FACC15" />

                    <View
                        android:layout_width="8dp"
                        android:layout_height="8dp"
                        android:layout_gravity="top|end"
                        android:layout_marginTop="2dp"
                        android:layout_marginEnd="6dp"
                        android:background="@drawable/bg_circle_yellow" />
                </FrameLayout>
            </LinearLayout>

            <!-- Greeting & User Info -->
            <LinearLayout
                android:id="@+id/ll_user_info"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:paddingHorizontal="24dp"
                android:layout_marginTop="32dp"
                app:layout_constraintTop_toBottomOf="@id/ll_top_bar">

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Good Morning,"
                    android:textColor="#D1FAE5"
                    android:textSize="16sp" />

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Sarah Anderson"
                    android:textColor="@color/white"
                    android:textSize="28sp"
                    android:textStyle="bold"
                    android:layout_marginTop="4dp"
                    android:letterSpacing="-0.02" />

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="16dp"
                    android:background="@drawable/bg_pill_status"
                    android:paddingHorizontal="16dp"
                    android:paddingVertical="8dp"
                    android:text="Treatment in Progress"
                    android:textColor="#D1FAE5"
                    android:textSize="14sp"
                    android:textStyle="bold" />
            </LinearLayout>

            <!-- Progress Card -->
            <com.google.android.material.card.MaterialCardView
                android:id="@+id/card_progress"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginHorizontal="24dp"
                android:layout_marginTop="40dp"
                app:cardCornerRadius="20dp"
                app:cardElevation="8dp"
                app:cardBackgroundColor="@color/white"
                app:strokeWidth="0dp"
                app:layout_constraintTop_toBottomOf="@id/ll_user_info">

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="vertical"
                    android:gravity="center"
                    android:paddingTop="36dp"
                    android:paddingBottom="32dp">

                    <FrameLayout
                        android:layout_width="150dp"
                        android:layout_height="150dp">

                        <ProgressBar
                            android:layout_width="match_parent"
                            android:layout_height="match_parent"
                            android:background="@drawable/bg_progress_circle"
                            android:indeterminate="false"
                            android:progress="33"
                            android:progressDrawable="@drawable/bg_progress_circle_green"
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
                                android:text="33%"
                                android:textColor="#059669"
                                android:textSize="36sp"
                                android:textStyle="bold" />

                            <TextView
                                android:layout_width="wrap_content"
                                android:layout_height="wrap_content"
                                android:text="COMPLETE"
                                android:textColor="#9CA3AF"
                                android:textSize="12sp"
                                android:textStyle="bold"
                                android:letterSpacing="0.05" />
                        </LinearLayout>
                    </FrameLayout>

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_marginTop="24dp"
                        android:text="Current Stage"
                        android:textColor="#374151"
                        android:textSize="20sp" />

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_marginTop="4dp"
                        android:text="Alignment Phase"
                        android:textColor="#9CA3AF"
                        android:textSize="14sp" />

                </LinearLayout>
            </com.google.android.material.card.MaterialCardView>

            <TextView
                android:id="@+id/tv_next_appointment_label"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginTop="32dp"
                android:layout_marginStart="24dp"
                android:text="NEXT APPOINTMENT"
                android:textColor="#9CA3AF"
                android:textSize="12sp"
                android:textStyle="bold"
                android:letterSpacing="0.05"
                app:layout_constraintTop_toBottomOf="@id/card_progress"
                app:layout_constraintStart_toStartOf="parent" />

            <!-- Appointment Card -->
            <com.google.android.material.card.MaterialCardView
                android:id="@+id/card_appointment"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginHorizontal="24dp"
                android:layout_marginTop="16dp"
                app:cardCornerRadius="16dp"
                app:cardElevation="0dp"
                app:cardBackgroundColor="@color/white"
                app:strokeWidth="1dp"
                app:strokeColor="#F3F4F6"
                app:layout_constraintTop_toBottomOf="@id/tv_next_appointment_label">

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="horizontal"
                    android:padding="16dp"
                    android:gravity="center_vertical">

                    <FrameLayout
                        android:layout_width="64dp"
                        android:layout_height="64dp"
                        android:background="@drawable/bg_calendar_gradient">
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
                                android:textSize="11sp"
                                android:textStyle="bold" />
                            <TextView
                                android:layout_width="wrap_content"
                                android:layout_height="wrap_content"
                                android:text="24"
                                android:textColor="@color/white"
                                android:textSize="22sp"
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
                            android:text="Regular Checkup"
                            android:textColor="#1F2937"
                            android:textSize="16sp"
                            android:textStyle="bold" />

                        <TextView
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:layout_marginTop="2dp"
                            android:text="10:30 AM • Dr. Smith"
                            android:textColor="#6B7280"
                            android:textSize="14sp" />
                    </LinearLayout>

                    <ImageView
                        android:layout_width="20dp"
                        android:layout_height="20dp"
                        android:src="@drawable/ic_chevron_right"
                        app:tint="#D1D5DB" />
                </LinearLayout>
            </com.google.android.material.card.MaterialCardView>

            <TextView
                android:id="@+id/tv_quick_actions_label"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginTop="32dp"
                android:layout_marginStart="24dp"
                android:text="QUICK ACTIONS"
                android:textColor="#9CA3AF"
                android:textSize="12sp"
                android:textStyle="bold"
                android:letterSpacing="0.05"
                app:layout_constraintTop_toBottomOf="@id/card_appointment"
                app:layout_constraintStart_toStartOf="parent"/>

            <!-- Quick Actions Grid -->
            <GridLayout
                android:id="@+id/grid_actions"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="16dp"
                android:layout_marginHorizontal="24dp"
                android:columnCount="2"
                android:rowCount="2"
                android:useDefaultMargins="false"
                app:layout_constraintTop_toBottomOf="@id/tv_quick_actions_label">

                <!-- Action 1: AI Assistant -->
                <com.google.android.material.card.MaterialCardView
                    android:id="@+id/card_ai_assistant"
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_columnWeight="1"
                    android:layout_marginEnd="8dp"
                    android:layout_marginBottom="16dp"
                    app:cardCornerRadius="16dp"
                    app:cardElevation="0dp"
                    app:cardBackgroundColor="@color/white"
                    app:strokeWidth="1dp"
                    app:strokeColor="#F3F4F6">
                    <LinearLayout
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:orientation="vertical"
                        android:gravity="center"
                        android:padding="24dp">
                        <FrameLayout
                            android:layout_width="56dp"
                            android:layout_height="56dp"
                            android:background="@drawable/bg_action_ai_gradient">
                            <ImageView
                                android:layout_width="28dp"
                                android:layout_height="28dp"
                                android:layout_gravity="center"
                                android:src="@drawable/ic_action_ai"
                                app:tint="@color/white" />
                        </FrameLayout>
                        <TextView
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:layout_marginTop="12dp"
                            android:text="AI Assistant"
                            android:textColor="#374151"
                            android:textSize="14sp" />
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
                    app:cardCornerRadius="16dp"
                    app:cardElevation="0dp"
                    app:cardBackgroundColor="@color/white"
                    app:strokeWidth="1dp"
                    app:strokeColor="#F3F4F6">
                    <LinearLayout
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:orientation="vertical"
                        android:gravity="center"
                        android:padding="24dp">
                        <FrameLayout
                            android:layout_width="56dp"
                            android:layout_height="56dp"
                            android:background="@drawable/bg_action_reminders_gradient">
                            <ImageView
                                android:layout_width="28dp"
                                android:layout_height="28dp"
                                android:layout_gravity="center"
                                android:src="@drawable/ic_action_reminders"
                                app:tint="@color/white" />
                        </FrameLayout>
                        <TextView
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:layout_marginTop="12dp"
                            android:text="Reminders"
                            android:textColor="#374151"
                            android:textSize="14sp" />
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
                    app:cardCornerRadius="16dp"
                    app:cardElevation="0dp"
                    app:cardBackgroundColor="@color/white"
                    app:strokeWidth="1dp"
                    app:strokeColor="#F3F4F6">
                    <LinearLayout
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:orientation="vertical"
                        android:gravity="center"
                        android:padding="24dp">
                        <FrameLayout
                            android:layout_width="56dp"
                            android:layout_height="56dp"
                            android:background="@drawable/bg_action_care_gradient">
                            <ImageView
                                android:layout_width="28dp"
                                android:layout_height="28dp"
                                android:layout_gravity="center"
                                android:src="@drawable/ic_action_care"
                                app:tint="@color/white" />
                        </FrameLayout>
                        <TextView
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:layout_marginTop="12dp"
                            android:text="Care Guide"
                            android:textColor="#374151"
                            android:textSize="14sp" />
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
                    app:cardCornerRadius="16dp"
                    app:cardElevation="0dp"
                    app:cardBackgroundColor="@color/white"
                    app:strokeWidth="1dp"
                    app:strokeColor="#F3F4F6">
                    <LinearLayout
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:orientation="vertical"
                        android:gravity="center"
                        android:padding="24dp">
                        <FrameLayout
                            android:layout_width="56dp"
                            android:layout_height="56dp"
                            android:background="@drawable/bg_action_report_gradient">
                            <ImageView
                                android:layout_width="28dp"
                                android:layout_height="28dp"
                                android:layout_gravity="center"
                                android:src="@drawable/ic_action_report"
                                app:tint="@color/white" />
                        </FrameLayout>
                        <TextView
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:layout_marginTop="12dp"
                            android:text="Report Issue"
                            android:textColor="#374151"
                            android:textSize="14sp" />
                    </LinearLayout>
                </com.google.android.material.card.MaterialCardView>

            </GridLayout>

        </androidx.constraintlayout.widget.ConstraintLayout>
    </androidx.core.widget.NestedScrollView>

    """ + bottom_nav_content

    with open(layout_file, 'w', encoding='utf-8') as f:
        f.write(new_content)

rewrite()
