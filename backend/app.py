from flask import Flask
from flask_cors import CORS
from routes.knowledge import knowledge_bp

def create_app():
    app = Flask(__name__)
    CORS(app)
    app.register_blueprint(knowledge_bp, url_prefix='/api')
    return app


