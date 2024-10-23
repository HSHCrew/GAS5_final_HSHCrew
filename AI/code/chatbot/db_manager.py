import sqlite3
import json

class MedicationDatabaseManager:
    def __init__(self, db_name='medication_management.db'):
        self.db_name = db_name

    def connect(self):
        return sqlite3.connect(self.db_name)

    def store_medication_info(self, user_id, medication_info):
        try:
            conn = self.connect()
            cursor = conn.cursor()
            cursor.execute('''
                CREATE TABLE IF NOT EXISTS medications (
                    user_id TEXT,
                    medication_info TEXT
                )
            ''')
            cursor.execute(
                "INSERT INTO medications (user_id, medication_info) VALUES (?, ?)",
                (user_id, json.dumps(medication_info))
            )
            conn.commit()
        except sqlite3.Error as e:
            print(f"Database error: {e}")
        finally:
            conn.close()

    def store_summary(self, user_id, summary):
        try:
            conn = self.connect()
            cursor = conn.cursor()
            cursor.execute('''
                CREATE TABLE IF NOT EXISTS summaries (
                    user_id TEXT,
                    summary TEXT
                )
            ''')
            cursor.execute(
                "INSERT INTO summaries (user_id, summary) VALUES (?, ?)",
                (user_id, summary)
            )
            conn.commit()
        except sqlite3.Error as e:
            print(f"Database error: {e}")
        finally:
            conn.close()
