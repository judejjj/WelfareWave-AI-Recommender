import firebase_admin
from firebase_admin import credentials, firestore

# 1. Authenticate
cred = credentials.Certificate("serviceAccountKey.json")
try:
    firebase_admin.get_app()
except ValueError:
    firebase_admin.initialize_app(cred)
db = firestore.client()

# 2. Targeted Schemes for AI Ranking
extra_schemes = [
    # --- STUDENTS ---
    {
        "title": "BPL Super-Scholarship",
        "category": "Students",
        "beneficiaryType": "Students",
        "eligibilityRules": "Income must be below ₹25,000 per year.",
        "benefits": "₹50,000 per year + laptop.",
        "applicationUrl": "https://example.com/bpl-scholarship"
    },
    {
        "title": "Merit Excellence Award",
        "category": "Students",
        "beneficiaryType": "Students",
        "eligibilityRules": "Open to all income groups with >95% marks.",
        "benefits": "One-time grant of ₹1,00,000.",
        "applicationUrl": "https://example.com/merit"
    },
    # --- FARMERS ---
    {
        "title": "Small Farmer Irrigation Grant",
        "category": "Farmers",
        "beneficiaryType": "Farmers",
        "eligibilityRules": "Income below ₹40,000. Less than 2 acres of land.",
        "benefits": "Free solar pump installation.",
        "applicationUrl": "https://example.com/irrigation"
    },
    {
        "title": "Hi-Tech Farming Subsidy",
        "category": "Farmers",
        "beneficiaryType": "Farmers",
        "eligibilityRules": "Income up to ₹5,00,000. Focus on greenhouse farming.",
        "benefits": "50% subsidy on greenhouse setup.",
        "applicationUrl": "https://example.com/hitech"
    },
    # --- SENIOR CITIZENS ---
    {
        "title": "Extreme Poverty Senior Pension",
        "category": "Senior Citizens",
        "beneficiaryType": "Senior Citizens",
        "eligibilityRules": "Age > 75. Income below ₹15,000.",
        "benefits": "Monthly pension of ₹3,000.",
        "applicationUrl": "https://example.com/extreme-pension"
    },
    # --- GENERAL ---
    {
        "title": "Middle-Class Housing Loan Subsidy",
        "category": "General",
        "beneficiaryType": "General",
        "eligibilityRules": "Income between ₹3,00,000 and ₹12,00,000.",
        "benefits": "3% interest subvention on home loans.",
        "applicationUrl": "https://example.com/housing"
    }
]

print("Adding extra schemes...")
for s in extra_schemes:
    s['timestamp'] = firestore.SERVER_TIMESTAMP
    db.collection('welfare_schemes').add(s)
    print(f"Added: {s['title']}")

print("Done! Now the database has enough variety for the AI to rank accurately.")