import os

class Config:
    SUPABASE_URL = os.getenv("SUPABASE_URL", "https://uxlztfbqrqfmzbzerkob.supabase.co")
    SUPABASE_KEY = os.getenv("SUPABASE_KEY", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InV4bHp0ZmJxcnFmbXpiemVya29iIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTA0NzM1OTMsImV4cCI6MjA2NjA0OTU5M30.b38e7I03Ri6CuClo-3kSX_zV9bUHitqV9pJwgzzBoW4")
