"""IMAGician development configuration."""
import pathlib

# Root of this application, useful if it doesn't occupy an entire domain
APPLICATION_ROOT = '/'

# Secret key for encrypting cookies
SECRET_KEY = b'\xab\xcd^\xb6\xd1\xb7\xc9\xc4\xbd\x82\
    \xfe\x1b/|\xdc\xd1s\x85!\xfb\x9b\xaa&?'
SESSION_COOKIE_NAME = 'login'

# File Upload to var/uploads/
IMAGician_ROOT = pathlib.Path(__file__).resolve().parent.parent
UPLOAD_FOLDER = IMAGician_ROOT/'var'/'uploads'
ALLOWED_EXTENSIONS = set(['png', 'jpg', 'jpeg', 'gif', 'pdf', 'zip'])
MAX_CONTENT_LENGTH = 16 * 1024 * 1024

# Database file is var/imagician.sqlite3
DATABASE_FILENAME = IMAGician_ROOT/'var'/'imagician.sqlite3'
