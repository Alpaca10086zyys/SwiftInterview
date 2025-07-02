from flask import Flask
from flask_cors import CORS
from routes.knowledge import knowledge_bp
from routes.user import user_bp
from routes.review import review_bp
from config import Config

def create_app():
    app = Flask(__name__)
    app.config.from_object(Config)

    CORS(app)
    app.register_blueprint(knowledge_bp)
    app.register_blueprint(user_bp)
    app.register_blueprint(review_bp)

    return app


