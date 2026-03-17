import firebase_admin
from firebase_admin import credentials, firestore, auth

try:
    firebase_admin.get_app()
except ValueError:
    cred = credentials.Certificate("serviceAccountKey.json")
    firebase_admin.initialize_app(cred)

db = firestore.client()

print("👤 Starting Strictly-Typed User Seeding...")

complex_users = [
    {
        "email": "sc_student@test.com", "password": "password", "name": "Rahul SC",
        "age": 20, "income": 45000, "category": "Students",
        "dob": "15/05/2005", "sex": "Male", "education": "Undergraduate",
        "relationshipStatus": "Unmarried", "caste": "SC", "familyMembers": 4,
        "fatherOccupation": "Daily Wager", "motherOccupation": "Homemaker",
        "hasGovtEmployee": False
    },
    {
        "email": "divorced_woman@test.com", "password": "password", "name": "Priya Female",
        "age": 35, "income": 30000, "category": "Women",
        "dob": "10/08/1990", "sex": "Female", "education": "12th Pass",
        "relationshipStatus": "Divorced", "caste": "OBC", "familyMembers": 2,
        "fatherOccupation": "Deceased", "motherOccupation": "Retired",
        "hasGovtEmployee": False
    },
    {
        "email": "govt_child@test.com", "password": "password", "name": "Arun Govt",
        "age": 21, "income": 600000, "category": "Students",
        "dob": "22/11/2004", "sex": "Male", "education": "Postgraduate",
        "relationshipStatus": "Unmarried", "caste": "General", "familyMembers": 3,
        "fatherOccupation": "Civil Servant", "motherOccupation": "Teacher",
        "hasGovtEmployee": True
    }
]

for user in complex_users:
    try:
        user_record = auth.create_user(
            email=user["email"], 
            password=user["password"], 
            display_name=user["name"]
        )
        
        # Data is now strictly typed for Java POJO mapping
        profile_data = {
            "name": user["name"], 
            "email": user["email"], 
            "age": user["age"],               # Integer
            "income": user["income"],         # Integer
            "category": user["category"], 
            "dob": user["dob"],
            "sex": user["sex"], 
            "education": user["education"],
            "relationshipStatus": user["relationshipStatus"], # Corrected Key
            "caste": user["caste"],
            "familyMembers": user["familyMembers"],           # Integer
            "fatherOccupation": user["fatherOccupation"],
            "motherOccupation": user["motherOccupation"],
            "hasGovtEmployee": user["hasGovtEmployee"],       # Boolean
            "createdAt": firestore.SERVER_TIMESTAMP
        }
        
        db.collection("user_profiles").document(user_record.uid).set(profile_data)
        print(f"✅ Created User perfectly: {user['email']}")
    except auth.EmailAlreadyExistsError:
        print(f"⚠️ User {user['email']} already exists. Delete them first.")
    except Exception as e:
        print(f"❌ Error with {user['email']}: {e}")

print("✅ User seeding finished!")