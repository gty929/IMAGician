"""imagician index (main) view.URLs include:/."""

import os
import pathlib
import uuid
import hashlib
import flask
from flask import abort
import arrow
import imagician
from imagician.model import get_db


def get_uuid_filename():
    """Get uuid filename."""
    # Unpack flask object
    fileobj = flask.request.files["file"]
    filename = fileobj.filename

    # Compute base name (filename without directory).  We use a UUID to avoid
    # clashes with existing files, and ensure that the name is compatible with
    # the filesystem.
    stem = uuid.uuid4().hex
    suffix = pathlib.Path(filename).suffix
    uuid_basename = f"{stem}{suffix}"

    # Save to disk
    file_pth = imagician.app.config["UPLOAD_FOLDER"]/uuid_basename
    return file_pth

@imagician.app.route('/accounts/login/', methods=['POST'])
def login():
    """Use this end point to login."""
    if 'username' not in flask.request.form \
            or 'password' not in flask.request.form:
        abort(400)
    username = flask.request.form['username']
    password = flask.request.form['password']
    
    connection = get_db()
    cur = connection.execute(
        "SELECT password, is_deleted FROM users WHERE username = ?",
        (username, )
    )
    result = cur.fetchall()
    if len(result) != 1 or result[0]["is_deleted"]:
        abort(403)
    stored_password = result[0]['password']
    algorithm = stored_password.split('$')[0]
    salt = stored_password.split('$')[1]
    encrypted_password = stored_password.split('$')[2]

    hash_obj = hashlib.new(algorithm)
    password_salted = salt + password
    hash_obj.update(password_salted.encode('utf-8'))
    password_hash = hash_obj.hexdigest()

    if password_hash != encrypted_password:
        abort(403)

    flask.session['username'] = username
    
    connection.execute(
        "UPDATE users SET last_login=datetime('now') WHERE username = ?",
        (username, )
    )
    context = {"Result": "Successfully logged in!"}
    context['username'] = username
    return flask.jsonify(**context)

@imagician.app.route('/accounts/logout/', methods=['POST'])
def logout():
    """Show logout."""
    # if logged in
    if 'username' in flask.session:
        flask.session.pop('username', None)
    context = {"Result": "Successfully logged out!"}
    return flask.jsonify(**context)

@imagician.app.route('/accounts/create/', methods=['POST'])
def create_account():
    """Create a new account."""
    # If already logged in, reject
    if 'username' in flask.session:
        abort(409)

    # Parse argument
    if 'username' not in flask.request.form \
        or 'password' not in flask.request.form \
        or 'fullname' not in flask.request.form \
        or 'email' not in flask.request.form \
            or 'phone_number' not in flask.request.form:
        abort(400)
    username = flask.request.form['username']
    password = flask.request.form['password']
    fullname = flask.request.form['fullname']
    email = flask.request.form['email']
    phone = flask.request.form['phone_number']
    
    # Sanity check: make sure username not exist
    connection = get_db()
    cur = connection.execute(
        "SELECT * FROM users WHERE username = ?",
        (username, )
    )
    result = cur.fetchall()
    if len(result) != 0:
        abort(409)
    
    new_password = encrypt_password(password)
    connection.execute(
        "INSERT INTO users"
        "(username, fullname, email, phone_number, password, is_deleted) "
        "VALUES (?,?,?,?,?,0)",
        (username, fullname, email, phone, new_password)
    )

    # initialize context
    context = {"Result": "Account successfully registered and logged in!"}
    context["username"] = username
    context["fullname"] = fullname
    context["email"] = email
    context["phone"] = phone
    flask.session['username'] = username
    return flask.jsonify(**context)

@imagician.app.route('/accounts/delete/', methods=['POST'])
def delete_account():
    """Delete an account."""
    # If not logged in, reject
    if 'username' not in flask.session:
        abort(403)
    logname = flask.session['username']
    
    # Sanity check: make sure username exists
    connection = get_db()
    cur = connection.execute(
        "SELECT * FROM users WHERE username = ?",
        (logname, )
    )
    result = cur.fetchall()
    if len(result) != 1 or result[0]["is_deleted"]:
        abort(403)
    connection.execute(
        "UPDATE users SET is_deleted = 1 "
        "WHERE username = ?",
        (logname, )
    ) 
    context = {"Result": "Account successfully deleted!"}
    context["Original_username"] = logname
    flask.session.pop('username', None)
    return flask.jsonify(**context)

@imagician.app.route('/accounts/edit/', methods=['POST'])
def edit_account():
    """Edit account."""
    # If not logged in, reject
    if 'username' not in flask.session:
        abort(403)
    logname = flask.session['username']

    # Connect to database
    connection = imagician.model.get_db()
    
    # Find user info
    cur = connection.execute(
        "SELECT DISTINCT * "
        "FROM users "
        "WHERE username = ?",
        (logname, )
    )
    user_info = cur.fetchall()
    if len(user_info) != 1 or user_info[0]["is_deleted"]:
        abort(403)
        
    user_info = user_info[0]
    if "fullname" not in flask.request.form:
        fullname = user_info['fullname']
    else:
        fullname = flask.request.form['fullname']
    
    if "email" not in flask.request.form:
        email = user_info['email']
    else:
        email = flask.request.form['email']
    
    if "phone_number" not in flask.request.form:
        phone_number = user_info['phone_number']
    else:
        phone_number = flask.request.form['phone_number']
    # Parse argument
    
    connection.execute(
        "UPDATE users SET fullname = ?, email = ?, phone_number = ? "
        "WHERE username = ?",
        (fullname, email, phone_number, logname)
    ) 
    
    context = {"Result": "Account info successfully updated!"}
    context["username"] = logname
    context["fullname"] = fullname
    context["email"] = email
    context["phone"] = phone_number
    return flask.jsonify(**context)

@imagician.app.route('/accounts/info/', methods=['GET'])
def get_account_info():
    """Get info of my account."""
    # If not logged in, reject
    if 'username' not in flask.session:
        abort(403)
    logname = flask.session['username']

    # Connect to database
    connection = imagician.model.get_db()
    
    # Find user info
    cur = connection.execute(
        "SELECT DISTINCT * "
        "FROM users "
        "WHERE username = ?",
        (logname, )
    )
    user_info = cur.fetchall()
    if len(user_info) != 1 or user_info[0]["is_deleted"]:
        abort(403)
        
    context = user_info[0]
    context["password"] = "[HIDDEN]"
    return flask.jsonify(**context)

@imagician.app.route("/uploads/<path:name>")
def download_file(name):
    """Download file."""
    if 'username' not in flask.session:
        abort(403)

    return flask.send_from_directory(
        imagician.app.config['UPLOAD_FOLDER'], name, as_attachment=True
    )

def encrypt_password(orig_pswd):
    """Encrypt password."""
    algorithm = 'sha512'
    salt = uuid.uuid4().hex
    hash_obj = hashlib.new(algorithm)
    password_salted = salt + orig_pswd
    hash_obj.update(password_salted.encode('utf-8'))
    password_hash = hash_obj.hexdigest()
    password_db_string = "$".join([algorithm, salt, password_hash])
    # print(password_db_string)
    return password_db_string