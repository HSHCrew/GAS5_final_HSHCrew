from medication_chatbot import MedicationChatbot

if __name__ == '__main__':
    chatbot = MedicationChatbot()

    user_id = 'user123'
    medication_info = {
        'medications': [
            {
                'name': 'Aspirin',
                'dosage': '100mg',
                'frequency': 'Once daily',
                'purpose': 'Blood thinner'
            },
            {
                'name': 'Metformin',
                'dosage': '500mg',
                'frequency': 'Twice daily',
                'purpose': 'Blood sugar control'
            }
        ],
        'health_conditions': [
            'Type 2 Diabetes',
            'Hypertension'
        ]
    }

    # chatbot.add_medication_info(user_id, medication_info)
    summary = chatbot.add_medication_info(user_id, medication_info)
    print("Medication Summary:")
    print(summary)

    while True:
        user_question = input("사용자 질문: ")
        if user_question.lower() in ['종료', 'exit', 'quit']:
            break
        response = chatbot.ask_question(user_id, user_question)
        print()
        print("AI 응답:")
        print(response)
        print()
