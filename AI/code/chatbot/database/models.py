from sqlalchemy import Column, Integer, String, Text, ForeignKey, DateTime
from sqlalchemy.orm import relationship
from sqlalchemy.sql import func
from sqlalchemy.ext.declarative import declarative_base

Base = declarative_base()

class UserProfile(Base):
    __tablename__ = "user_profile"
    
    user_profile_id = Column(Integer, primary_key=True)
    # 다른 필요한 user_profile 컬럼들...
    
    # relationship 정의
    medications = relationship("UserMedication", back_populates="user")

class UserMedication(Base):
    __tablename__ = "user_medication"
    
    user_medication_id = Column(Integer, primary_key=True)
    user_profile_id = Column(Integer, ForeignKey('user_profile.user_profile_id'))
    medication_id = Column(Integer, ForeignKey('medication.medication_id'))
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    updated_at = Column(DateTime(timezone=True), onupdate=func.now())
    
    # relationships 정의
    user = relationship("UserProfile", back_populates="medications")
    medication = relationship("Medication", back_populates="user_medications")

class Medication(Base):
    __tablename__ = "medication"
    
    medication_id = Column(Integer, primary_key=True)
    medication_name = Column(String(255))
    medication_caution_info = Column(Text)
    medication_storage_method_info = Column(Text)
    medication_interaction_info = Column(Text)
    medication_caution_warning_info = Column(Text)
    medication_efficacy_info = Column(Text)
    medication_item_dur = Column(Text)
    medication_se_info = Column(Text)
    medication_use_info = Column(Text)
    taking_info = Column(Text)
    ingredient = Column(Text)
    
    # relationship 정의
    user_medications = relationship("UserMedication", back_populates="medication")

class MedicationSummary(Base):
    __tablename__ = "medication_summary"
    __table_args__ = {'mysql_charset': 'utf8mb4', 'mysql_collate': 'utf8mb4_unicode_ci'}
    
    medication_summary_id = Column(Integer, primary_key=True)
    medication_summary_created_at = Column(DateTime(timezone=True), server_default=func.now())
    medication_summary_updated_at = Column(DateTime(timezone=True), onupdate=func.now())
    failed = Column(Text)
    fewshots = Column(Text)
    restructured = Column(Text)
    summary = Column(Text)
    medication_id = Column(Integer, ForeignKey('medication.medication_id'))
    user_profile_id = Column(Integer, ForeignKey('user_profile.user_profile_id'))
    
    # relationships
    medication = relationship("Medication")
    user = relationship("UserProfile")