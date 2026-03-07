import os
import re

with open('app/src/main/res/layout/activity_create_clinician.xml', 'r', encoding='utf-8') as f:
    xml = f.read()

xml = xml.replace('create_clinician_root', 'create_patient_root')
xml = xml.replace('CreateClinicianActivity', 'CreatePatientActivity')
xml = xml.replace('Create Clinician Account', 'Create Patient Account')
xml = xml.replace('CLINICIAN ID', 'PATIENT ID')
xml = xml.replace('til_clinician_id', 'til_patient_id')
xml = xml.replace('et_clinician_id', 'et_patient_id')
xml = xml.replace('DR005', 'PT005')
xml = xml.replace('Dr. Jane Doe', 'John Doe')
xml = xml.replace('FULL NAME', 'PATIENT NAME')
xml = xml.replace('et_full_name', 'et_patient_name')
xml = xml.replace('ROLE', 'EMAIL (OPTIONAL)')
xml = xml.replace('ic_stethoscope', 'ic_mail_outline')
xml = xml.replace('et_role', 'et_patient_email')
xml = xml.replace('Orthodontist', 'john@example.com')
xml = xml.replace('by the clinician', 'by the patient')
xml = xml.replace('AutoCompleteTextView', 'com.google.android.material.textfield.TextInputEditText')
xml = xml.replace('inputType="none"', 'inputType="textEmailAddress"')
xml = xml.replace('style="@style/Widget.MaterialComponents.TextInputLayout.OutlinedBox.ExposedDropdownMenu"\n', '')
xml = re.sub(r'android:popupBackground="@color/white"\n.*?android:dropDownSelector="@android:color/transparent"', '', xml, flags=re.DOTALL)
xml = xml.replace('Update Clinician', 'Update Patient')

with open('app/src/main/res/layout/activity_create_patient.xml', 'w', encoding='utf-8') as f:
    f.write(xml)

with open('app/src/main/java/com/yourname/orthoguide/CreateClinicianActivity.kt', 'r', encoding='utf-8') as f:
    kt = f.read()

kt = kt.replace('CreateClinicianActivity', 'CreatePatientActivity')
kt = kt.replace('activity_create_clinician', 'activity_create_patient')
kt = kt.replace('create_clinician_root', 'create_patient_root')
kt = kt.replace('et_clinician_id', 'et_patient_id')
kt = kt.replace('et_full_name', 'et_patient_name')
kt = kt.replace('et_role', 'et_patient_email')
kt = kt.replace('clinicianId', 'patientId')
kt = kt.replace('clinicianName', 'patientName')
kt = kt.replace('clinicianRole', 'patientEmail')
kt = kt.replace('clinicianPhone', 'patientPhone')
kt = kt.replace('Orthodontist', '')
kt = kt.replace('Clinician', 'Patient')
kt = kt.replace('AutoCompleteTextView', 'TextInputEditText')
kt = re.sub(r'override fun onResume\(\) \{.*?\}\n', '', kt, flags=re.DOTALL) # remove overriding onResume
kt = kt.replace('import android.widget.ArrayAdapter', '')

with open('app/src/main/java/com/yourname/orthoguide/CreatePatientActivity.kt', 'w', encoding='utf-8') as f:
    f.write(kt)
