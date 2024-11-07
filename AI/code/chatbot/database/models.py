from sqlalchemy import Column, Integer, String, ForeignKey, DateTime, func
from sqlalchemy.orm import declarative_base

Base = declarative_base()

class User(Base):
    __tablename__ = "users"
    id = Column(Integer, primary_key=True)
    name = Column(String(50))

class Medication(Base):
    __tablename__ = "medications"
    id = Column(Integer, primary_key=True)
    name = Column(String(100))
    details = Column(String)
    dur_info = Column(String)

class MedicationSummary(Base):
    __tablename__ = "medication_summaries"
    
    user_id = Column(Integer, ForeignKey("users.id"), primary_key=True)
    medication_id = Column(Integer, ForeignKey("medications.id"), primary_key=True)
    index = Column(Integer, nullable=True)
    restructured = Column(String)
    summary = Column(String)
    fewshots = Column(String)
    failed = Column(String)
    last_updated = Column(DateTime, server_default=func.now(), onupdate=func.now())
