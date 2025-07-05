from flask import Flask
from flask_cors import CORS
from routes.knowledge import knowledge_bp
from routes.user import user_bp
from routes.review import review_bp
from config import Config
from routes.interview import interview_bp
from routes.interview_text import interview_text_bp
from routes.review import review_bp

def create_app():
    app = Flask(__name__)
    app.config.from_object(Config)

    CORS(app)
    app.register_blueprint(knowledge_bp)
    app.register_blueprint(user_bp)
    app.register_blueprint(interview_bp)
    app.register_blueprint(interview_text_bp)
    app.register_blueprint(review_bp)

    return app


