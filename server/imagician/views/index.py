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


def query_db(query, args=(), one=False):
    """Query DB."""
    cur = get_db().execute(query, args)
    r_v = cur.fetchall()
    cur.close()  # Whether need to close or not?
    return (r_v[0] if r_v else None) if one else r_v


@imagician.app.route('/')
def show_home():
    """Display / route."""
    # If not logged in, redirect
    if 'username' not in flask.session:
        return flask.redirect('/accounts/login/')
    logname = flask.session['username']

    # Connect to database
    connection = imagician.model.get_db()

    # Query database
    cur = connection.execute(
        "SELECT DISTINCT p.postid, p.filename, p.owner, p.created "
        "FROM posts p, following f "
        "WHERE p.owner = ? OR p.owner = f.username2 "
        "AND f.username1 = ? ORDER BY postid DESC",
        (logname, logname, )
    )
    posts = cur.fetchall()

    for post in posts:
        post['img_url'] = '/uploads/' + post['filename']
        unreadable_time = arrow.get(post['created'], 'YYYY-MM-DD HH:mm:ss')
        post['timestamp'] = unreadable_time.humanize()

        # Find owner image
        cur = connection.execute(
            "SELECT DISTINCT filename "
            "FROM users "
            "WHERE username = ?",
            (post['owner'], )
        )
        owner_info = cur.fetchall()
        post['owner_img_url'] = '/uploads/' + owner_info[0]['filename']
        # Find num of likes
        cur = connection.execute(
            "SELECT DISTINCT likeid "
            "FROM likes "
            "WHERE postid = ?",
            (post['postid'], )
        )
        post['likes'] = len(cur.fetchall())

        # Check whether the user has liked the post
        cur = connection.execute(
            "SELECT DISTINCT likeid "
            "FROM likes "
            "WHERE postid = ? AND owner = ?",
            (post['postid'], logname, )
        )
        post['user_liked'] = len(cur.fetchall()) > 0

        # Find all comments
        cur = connection.execute(
            "SELECT DISTINCT owner, text, commentid "
            "FROM comments "
            "WHERE postid = ? "
            "ORDER BY commentid",
            (post['postid'], )
        )
        post['comments'] = cur.fetchall()

    context = {"logname": logname, "posts": posts}

    return flask.render_template("home.html", **context)


@imagician.app.route('/users/<path:username>/')
def show_user_homepage(username):
    """Display /users/<user_url_slug>/ route."""
    # If not logged in, redirect
    if 'username' not in flask.session:
        return flask.redirect('/accounts/login/')
    logname = flask.session['username']

    # Connect to database
    connection = imagician.model.get_db()

    # Sanity check
    cur = connection.execute(
        "SELECT DISTINCT fullname "
        "FROM users "
        "WHERE username = ?",
        (username, )
    )
    if len(cur.fetchall()) == 0:
        flask.abort(404)

    # initialize context
    context = {"logname": logname, "username": username}

    # Check following
    cur = connection.execute(
        "SELECT DISTINCT username1, username2 "
        "FROM following "
        "WHERE username1 = ? AND username2 = ?",
        (logname, username, )
    )
    context['logname_follows_username'] = len(cur.fetchall()) > 0

    # Find user full name
    cur = connection.execute(
        "SELECT DISTINCT fullname "
        "FROM users "
        "WHERE username = ?",
        (username, )
    )
    context['fullname'] = cur.fetchall()[0]['fullname']

    # Find num following
    cur = connection.execute(
        "SELECT DISTINCT username1, username2 "
        "FROM following "
        "WHERE username1 = ?",
        (username, )
    )
    context['following'] = len(cur.fetchall())

    # Find num followers
    cur = connection.execute(
        "SELECT DISTINCT username1, username2 "
        "FROM following "
        "WHERE username2 = ?",
        (username, )
    )
    context['followers'] = len(cur.fetchall())

    # Find all the posts
    cur = connection.execute(
        "SELECT DISTINCT postid, filename as img_url "
        "FROM posts "
        "WHERE owner = ? "
        "ORDER BY postid",
        (username, )
    )
    posts = cur.fetchall()
    for post in posts:
        post['img_url'] = '/uploads/' + post['img_url']
    context['total_posts'] = len(posts)
    context['posts'] = posts

    return flask.render_template("user.html", **context)


@imagician.app.route('/users/<path:username>/followers/')
def show_user_followers(username):
    """Display /users/<user_url_slug>/followers/ route."""
    # If not logged in, redirect
    if 'username' not in flask.session:
        return flask.redirect('/accounts/login/')
    logname = flask.session['username']

    # Connect to database
    connection = imagician.model.get_db()

    # Sanity check
    cur = connection.execute(
        "SELECT DISTINCT fullname "
        "FROM users "
        "WHERE username = ?",
        (username, )
    )
    if len(cur.fetchall()) == 0:
        flask.abort(404)

    # initialize context
    context = {"logname": logname, "username": username}

    # Find all followers
    cur = connection.execute(
        "SELECT DISTINCT username1 as username "
        "FROM following "
        "WHERE username2 = ?",
        (username, )
    )
    followers = cur.fetchall()

    for follower in followers:
        # Find follower image
        cur = connection.execute(
            "SELECT DISTINCT filename as user_img_url "
            "FROM users "
            "WHERE username = ?",
            (follower['username'], )
        )
        follower['user_img_url'] = '/uploads/' \
            + cur.fetchall()[0]['user_img_url']

        # Check following relationship with logname
        cur = connection.execute(
            "SELECT DISTINCT username2 "
            "FROM following "
            "WHERE username1 = ? AND username2 = ?",
            (logname, follower['username'], )
        )
        follower['logname_follows_username'] = len(cur.fetchall()) > 0

    context['followers'] = followers
    return flask.render_template("followers.html", **context)


@imagician.app.route('/users/<path:username>/following/')
def show_user_following(username):
    """Display /users/<user_url_slug>/following/ route."""
    # If not logged in, redirect
    if 'username' not in flask.session:
        return flask.redirect('/accounts/login/')
    logname = flask.session['username']

    # Connect to database
    connection = imagician.model.get_db()

    # Sanity check
    cur = connection.execute(
        "SELECT DISTINCT fullname "
        "FROM users "
        "WHERE username = ?",
        (username, )
    )
    if len(cur.fetchall()) == 0:
        flask.abort(404)

    # Initialize context
    context = {"logname": logname, "username": username}

    # Find all followings
    cur = connection.execute(
        "SELECT DISTINCT username2 as username "
        "FROM following "
        "WHERE username1 = ?",
        (username, )
    )
    followings = cur.fetchall()

    for following in followings:
        # Find following image
        cur = connection.execute(
            "SELECT DISTINCT filename as user_img_url "
            "FROM users "
            "WHERE username = ?",
            (following['username'], )
        )
        following['user_img_url'] = '/uploads/' + \
            cur.fetchall()[0]['user_img_url']

        # Check following relationship with logname
        cur = connection.execute(
            "SELECT DISTINCT username2 "
            "FROM following "
            "WHERE username1 = ? AND username2 = ?",
            (logname, following['username'], )
        )
        following['logname_follows_username'] = len(cur.fetchall()) > 0

    context['following'] = followings
    return flask.render_template("following.html", **context)


@imagician.app.route('/posts/<path:postid>/')
def show_post(postid):
    """Display /posts/<postid_url_slug>/ route."""
    # If not logged in, redirect
    if 'username' not in flask.session:
        return flask.redirect('/accounts/login/')
    logname = flask.session['username']

    # Connect to database
    connection = imagician.model.get_db()

    # Initialize context
    context = {"logname": logname}

    # Query database
    cur = connection.execute(
        "SELECT DISTINCT p.postid, p.filename, p.owner, p.created "
        "FROM posts p "
        "WHERE p.postid = ?",
        (postid, )
    )
    post = cur.fetchall()
    if len(post) == 0:
        flask.abort(404)

    post = post[0]
    context['postid'] = postid
    context['owner'] = post['owner']
    context['img_url'] = '/uploads/' + post['filename']
    unreadable_time = arrow.get(post['created'], 'YYYY-MM-DD HH:mm:ss')
    context['timestamp'] = unreadable_time.humanize()

    # Find owner image
    cur = connection.execute(
        "SELECT DISTINCT filename "
        "FROM users "
        "WHERE username = ?",
        (post['owner'], )
    )
    owner_info = cur.fetchall()
    context['owner_img_url'] = '/uploads/' + owner_info[0]['filename']
    # Find num of likes
    cur = connection.execute(
        "SELECT DISTINCT likeid "
        "FROM likes "
        "WHERE postid = ?",
        (post['postid'], )
    )
    context['likes'] = len(cur.fetchall())

    # Check whether the user has liked the post
    cur = connection.execute(
        "SELECT DISTINCT likeid "
        "FROM likes "
        "WHERE postid = ? AND owner = ?",
        (post['postid'], logname, )
    )
    context['user_liked'] = len(cur.fetchall()) > 0

    # Find all comments
    cur = connection.execute(
        "SELECT DISTINCT owner, text, commentid "
        "FROM comments "
        "WHERE postid = ? "
        "ORDER BY commentid",
        (post['postid'], )
    )
    comments = cur.fetchall()

    for comment in comments:
        # Check whether the user owns the comment
        if comment['owner'] == logname:
            comment['user_owned'] = True
        else:
            comment['user_owned'] = False

    context['comments'] = comments
    return flask.render_template("post.html", **context)


@imagician.app.route('/explore/')
def show_explore():
    """Display /explore/ route."""
    # If not logged in, redirect
    if 'username' not in flask.session:
        return flask.redirect('/accounts/login/')
    logname = flask.session['username']

    # Connect to database
    connection = imagician.model.get_db()

    # Initialize context
    context = {"logname": logname}

    # Find all not followings
    cur = connection.execute(
        "SELECT DISTINCT u.username "
        "FROM users u "
        "WHERE u.username NOT IN "
        "(SELECT DISTINCT f.username2 AS username "
        "FROM following f "
        "WHERE f.username1 = ?) AND u.username <> ?",
        (logname, logname, )
    )
    not_followings = cur.fetchall()

    for not_following in not_followings:
        # Find not following image
        cur = connection.execute(
            "SELECT DISTINCT filename "
            "FROM users "
            "WHERE username = ?",
            (not_following['username'], )
        )
        not_following['user_img_url'] = '/uploads/' + \
            cur.fetchall()[0]['filename']

    context['not_following'] = not_followings
    return flask.render_template("explore.html", **context)


@imagician.app.route('/accounts/login/', methods=['GET'])
def show_login():
    """Display /accounts/login/ route."""
    # If already logged in, redirect
    if 'username' in flask.session:
        return flask.redirect(flask.url_for('show_home'))

    # initialize context
    context = {}

    return flask.render_template("login.html", **context)


@imagician.app.route('/accounts/logout/', methods=['POST'])
def show_logout():
    """Show logout."""
    # if logged in
    if 'username' in flask.session:
        flask.session.pop('username', None)
    return flask.redirect(flask.url_for('show_login'))


@imagician.app.route('/accounts/create/', methods=['GET'])
def show_create():
    """Show create."""
    # If already logged in, redirect
    if 'username' in flask.session:
        return flask.redirect(flask.url_for('show_edit'))

    # initialize context
    context = {}

    return flask.render_template("create.html", **context)


@imagician.app.route('/accounts/delete/')
def show_delete():
    """Show delete."""
    # If not logged in, redirect
    if 'username' not in flask.session:
        return flask.redirect('/accounts/login/')
    logname = flask.session['username']

    # Initialize context
    context = {"logname": logname}

    return flask.render_template("delete.html", **context)


@imagician.app.route('/accounts/edit/')
def show_edit():
    """Show edit."""
    # If not logged in, redirect
    if 'username' not in flask.session:
        return flask.redirect('/accounts/login/')
    logname = flask.session['username']

    # Connect to database
    connection = imagician.model.get_db()

    # Initialize context
    context = {"logname": logname}

    # Find user info
    cur = connection.execute(
        "SELECT DISTINCT fullname, email, filename "
        "FROM users "
        "WHERE username = ?",
        (logname, )
    )
    user_info = cur.fetchall()[0]
    context['fullname'] = user_info['fullname']
    context['email'] = user_info['email']
    context['user_img_url'] = '/uploads/' + user_info['filename']

    return flask.render_template("edit.html", **context)


@imagician.app.route('/accounts/password/')
def show_password():
    """Show password."""
    # If not logged in, redirect
    if 'username' not in flask.session:
        return flask.redirect('/accounts/login/')
    logname = flask.session['username']

    # Initialize context
    context = {"logname": logname}

    return flask.render_template("password.html", **context)


@imagician.app.route("/likes/", methods=["POST"])
def postonly_likes():
    """Process like/unlike requests."""
    # If not logged in, redirect
    if 'username' not in flask.session:
        return flask.redirect('/accounts/login/')
    # Get target URL
    if 'target' not in flask.request.args:
        target = '/'
    else:
        target = flask.request.args['target']

    # Connect to Database
    connection = get_db()

    # Get request information
    username = flask.session['username']
    operation = flask.request.form['operation']
    postid = flask.request.form['postid']
    cur = connection.execute(
        "SELECT DISTINCT owner "
        "FROM likes "
        "WHERE owner = ? AND postid = ?",
        (username, postid, )
    )
    if operation == 'like':
        # LIKE request
        # sanity check
        if len(cur.fetchall()) != 0:
            abort(409)
        # insert like
        query_db('INSERT INTO likes (owner, postid) VALUES (?, ?)',
                 (username, postid, ))
    elif operation == 'unlike':
        # UNLIKE request
        # sanity check
        if len(cur.fetchall()) == 0:
            abort(409)
        # delete like
        query_db('DELETE FROM likes WHERE owner = ? AND postid = ?',
                 (username, postid, ))
    # Redirect
    return flask.redirect(target)


@imagician.app.route("/comments/", methods=["POST"])
def postonly_comments():
    """Create/delete comments."""
    # If not logged in, redirect
    if 'username' not in flask.session:
        return flask.redirect('/accounts/login/')

    # Get target URL
    if 'target' not in flask.request.args:
        target = '/'
    else:
        target = flask.request.args['target']

    # Connect to Database
    connection = get_db()

    # Get request form/session information
    username = flask.session['username']
    operation = flask.request.form['operation']

    # Create comment
    if operation == 'create':
        postid = flask.request.form['postid']
        text = flask.request.form['text']
        # Raise empty comments
        if text is None:
            abort(400)
        query_db('INSERT INTO comments (owner, postid, text) \
                    VALUES (?, ?, ?)', (username, postid, text, ))
    # Delete comment
    elif operation == 'delete':
        commentid = flask.request.form['commentid']
        cur = connection.execute(
            "SELECT DISTINCT owner "
            "FROM comments "
            "WHERE commentid =  ?",
            (commentid, )
        )
        result = cur.fetchall()
        if len(result) == 0:
            abort(403)
        comment_owner = result[0]['owner']
        if comment_owner != username:
            abort(403)
        query_db('DELETE FROM comments WHERE commentid = ?', (commentid, ))
    # Redirect
    return flask.redirect(target)


@imagician.app.route("/posts/", methods=["POST"])
def postonly_posts():
    """Process create/delete posts."""
    # If not logged in, redirect
    if 'username' not in flask.session:
        return flask.redirect('/accounts/login/')

    # Connect to Database
    connection = get_db()

    # Get request information
    username = flask.session['username']
    operation = flask.request.form['operation']

    if operation == "create":
        img_file = flask.request.files['file']
        # sanity check
        if img_file is None:
            abort(400)

        filename = img_file.filename
        # Compute base name (filename without directory).  We use a UUID to
        # avoid clashes with existing files, and ensure that the name is
        # compatible with the filesystem.
        stem = uuid.uuid4().hex
        suffix = pathlib.Path(filename).suffix
        uuid_basename = f"{stem}{suffix}"

        # Save to disk
        file_pth = imagician.app.config["UPLOAD_FOLDER"]/uuid_basename

        img_file.save(file_pth)
        query_db('INSERT INTO posts (owner, filename) \
                    VALUES (?, ?)', (username, uuid_basename, ))
    elif operation == "delete":
        postid = flask.request.form['postid']
        cur = connection.execute(
            "SELECT DISTINCT owner "
            "FROM posts "
            "WHERE postid =  ?",
            (postid, )
        )
        result = cur.fetchall()
        # sanity check
        if len(result) == 0 or result[0]['owner'] != username:
            abort(403)
        trg_post = query_db(
            'SELECT * FROM posts WHERE postid = ?', (postid, ), one=True)
        img_file = trg_post['filename']
        os.remove(os.path.join(imagician.app.config["UPLOAD_FOLDER"], img_file))
        query_db('DELETE FROM posts WHERE postid = ?', (postid, ))

    # Redirect
    if 'target' not in flask.request.args:
        target = '/users/' + username + '/'
    else:
        target = flask.request.args['target']
    return flask.redirect(target)


@imagician.app.route("/following/", methods=["POST"])
def postonly_following():
    """Process create/delete following."""
    # If not logged in, redirect
    if 'username' not in flask.session:
        return flask.redirect('/accounts/login/')
    # Get target URL
    if 'target' not in flask.request.args:
        target = '/'
    else:
        target = flask.request.args['target']

    # Connect to Database
    connection = get_db()

    # Get request information
    logname = flask.session['username']
    operation = flask.request.form['operation']
    username = flask.request.form['username']
    cur = connection.execute(
        "SELECT * "
        "FROM following "
        "WHERE username1 = ? AND username2 = ?",
        (logname, username, )
    )
    result = cur.fetchall()
    if operation == 'follow':
        # FOLLOW request
        # sanity check
        if len(result) != 0:
            abort(409)
        # insert follow
        query_db('INSERT INTO following (username1,username2) VALUES (?,?)',
                 (logname, username, ))
    elif operation == 'unfollow':
        # UNFOLLOW request
        # sanity check
        if len(result) == 0:
            abort(409)
        # delete follow
        query_db('DELETE FROM following WHERE username1 = ? AND username2 = ?',
                 (logname, username, ))
    # Redirect
    return flask.redirect(target)


@imagician.app.route("/accounts/", methods=["POST"])
def postonly_accounts():
    """Deal with post requests regarding accounts."""
    # Get request information
    operation = flask.request.form['operation']

    if operation == 'login':
        if 'username' not in flask.request.form \
                or 'password' not in flask.request.form:
            abort(400)
        username = flask.request.form['username']
        password = flask.request.form['password']
        login_helper(logname=username, password=password)

    if operation == 'create':
        print(flask.request.form)
        if 'username' not in flask.request.form \
            or 'password' not in flask.request.form \
            or 'fullname' not in flask.request.form \
           or 'email' not in flask.request.form \
                or 'file' not in flask.request.files:
            print("not full")
            abort(400)
        username = flask.request.form['username']
        password = flask.request.form['password']
        fullname = flask.request.form['fullname']
        email = flask.request.form['email']
        file = flask.request.files['file']
        create_helper(username, password, fullname, email, file)

    if operation == 'delete':
        if 'username' not in flask.session:
            abort(403)
        logname = flask.session['username']
        delete_helper(logname)

    if operation == 'edit_account':
        print(flask.request.form)
        if 'username' not in flask.session:
            abort(403)
        if 'fullname' not in flask.request.form \
                or 'email' not in flask.request.form:
            abort(400)
        logname = flask.session['username']
        fullname = flask.request.form['fullname']
        email = flask.request.form['email']
        edit_helper(logname, fullname, email)

    if operation == 'update_password':
        update_password_helper()

    # Get target URL
    if 'target' not in flask.request.args:
        target = '/'
    else:
        target = flask.request.args['target']
    return flask.redirect(target)


def login_helper(logname, password):
    """Login helper."""
    connection = get_db()
    cur = connection.execute(
        "SELECT password FROM users WHERE username = ?",
        (logname, )
    )
    result = cur.fetchall()
    if len(result) != 1:
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

    flask.session['username'] = logname


def create_helper(username, password, fullname, email, file):
    """Create helper."""
    if username in [x['username'] for x in query_db('SELECT * FROM users')]:
        abort(409)
    new_password = encrypt_password(password)

    filename = file.filename
    stem = uuid.uuid4().hex
    suffix = pathlib.Path(filename).suffix
    uuid_basename = f"{stem}{suffix}"

    # Save to disk
    file_pth = imagician.app.config["UPLOAD_FOLDER"]/uuid_basename

    file.save(file_pth)
    query_db('INSERT INTO users(username, fullname, email, filename, \
                password) VALUES (?,?,?,?,?)',
             (username, fullname, email, uuid_basename, new_password))
    login_helper(username, password)


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


def delete_helper(username):
    """Delete helper."""
    # Delete sessions ON DELETE CASCADE
    trg_posts = query_db('SELECT * FROM posts WHERE owner = ?', (username, ))
    for post in trg_posts:
        img_file = post['filename']
        img_pth = os.path.join(imagician.app.config["UPLOAD_FOLDER"], img_file)
        os.remove(img_pth)
    query_db('DELETE FROM posts WHERE owner = ?', (username, ))

    # Delete user account
    trg_user_img_file = query_db(
        'SELECT DISTINCT filename FROM users WHERE username = ?', (username, ))
    img_pth = os.path.join(
        imagician.app.config["UPLOAD_FOLDER"], trg_user_img_file[0]['filename'])
    os.remove(img_pth)
    query_db('DELETE FROM users WHERE username = ?', (username, ))

    flask.session.clear()


def edit_helper(logname, fullname, email):
    """Edit helper."""
    if 'file' not in flask.request.files:
        query_db('UPDATE users SET fullname = ?, email = ? WHERE username = ?',
                 (fullname, email, logname))
    else:
        connection = get_db()
        cur = connection.execute(
            "SELECT * "
            "FROM users "
            "WHERE username = ?",
            (logname, )
        )
        old_file = cur.fetchall()[0]['filename']
        old_file = os.path.join(
            imagician.app.config["UPLOAD_FOLDER"], old_file
        )
        os.remove(old_file)

        new_file = flask.request.files['file']
        filename = new_file.filename
        # Compute base name (filename without directory).  We use a UUID to
        # avoid clashes with existing files, and ensure that the name is
        # compatible with the filesystem.
        stem = uuid.uuid4().hex
        suffix = pathlib.Path(filename).suffix
        uuid_basename = f"{stem}{suffix}"

        # Save to disk
        file_pth = imagician.app.config["UPLOAD_FOLDER"]/uuid_basename

        new_file.save(file_pth)
        query_db('UPDATE users SET fullname = ?, email = ?, \
                 filename = ? WHERE username = ?',
                 (fullname, email, uuid_basename, logname))


def update_password_helper():
    """Update password helper."""
    if 'username' not in flask.session:
        abort(403)
    if 'password' not in flask.request.form \
            or 'new_password1' not in flask.request.form \
            or 'new_password2' not in flask.request.form:
        abort(400)
    logname = flask.session['username']
    password = flask.request.form['password']
    new_password1 = flask.request.form['new_password1']
    new_password2 = flask.request.form['new_password2']
    if password is None or password == '':
        abort(403)

    if new_password1 is None or new_password1 == '':
        abort(403)

    if new_password2 is None or new_password2 == '':
        abort(403)

    connection = get_db()
    cur = connection.execute(
        "SELECT password FROM users WHERE username = ?",
        (logname, )
    )
    result = cur.fetchall()
    if len(result) != 1:
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

    if new_password1 != new_password2:
        abort(401)

    new_password_db_string = encrypt_password(new_password1)

    query_db('UPDATE users SET password = ? WHERE username = ?',
             (new_password_db_string, logname))


@imagician.app.route("/uploads/<path:name>")
def download_file(name):
    """Download file."""
    if 'username' not in flask.session:
        abort(403)

    return flask.send_from_directory(
        imagician.app.config['UPLOAD_FOLDER'], name, as_attachment=True
    )
