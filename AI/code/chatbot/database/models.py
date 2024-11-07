from sqlalchemy import Column, Integer, String, ForeignKey, DateTime, func, Text, UniqueConstraint
from sqlalchemy.orm import declarative_base, relationship

Base = declarative_base()

class User(Base):
    __tablename__ = "users"
    
    id = Column(Integer, primary_key=True)
    name = Column(String(50))
    created_at = Column(DateTime, server_default=func.now())
    
    medications = relationship("UserMedication", back_populates="user")
    summaries = relationship("MedicationSummary", back_populates="user")

class Medication(Base):
    __tablename__ = "medications"
    
    id = Column(Integer, primary_key=True)
    name = Column(String(100), nullable=False)
    details = Column(Text)  # JSON 형식의 상세 정보
    dur_info = Column(Text)  # JSON 형식의 DUR 정보
    created_at = Column(DateTime, server_default=func.now())
    
    user_medications = relationship("UserMedication", back_populates="medication")
    summaries = relationship("MedicationSummary", back_populates="medication")

class UserMedication(Base):
    __tablename__ = "user_medications"
    
    id = Column(Integer, primary_key=True)
    user_id = Column(Integer, ForeignKey('users.id'), nullable=False)
    medication_id = Column(Integer, ForeignKey('medications.id'), nullable=False)
    created_at = Column(DateTime, server_default=func.now())
    
    user = relationship("User", back_populates="medications")
    medication = relationship("Medication", back_populates="user_medications")

class MedicationSummary(Base):
    __tablename__ = "medication_summaries"
    
    id = Column(Integer, primary_key=True)
    user_id = Column(Integer, ForeignKey('users.id'), nullable=False)
    medication_id = Column(Integer, ForeignKey('medications.id'), nullable=False)
    restructured = Column(Text)
    summary = Column(Text)
    fewshots = Column(Text)
    failed = Column(Text)
    created_at = Column(DateTime, server_default=func.now())
    last_updated = Column(DateTime, server_default=func.now(), onupdate=func.now())
    
    # 유니크 제약조건 추가
    __table_args__ = (
        UniqueConstraint('user_id', 'medication_id', name='uix_user_medication'),
    )
    
    user = relationship("User", back_populates="summaries")
    medication = relationship("Medication", back_populates="summaries")