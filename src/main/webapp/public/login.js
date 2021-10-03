document.addEventListener('DOMContentLoaded', function () {
    let login = document.querySelector("#login");
    
    login.querySelector('.btn-submit').addEventListener('click', async function (e) {
        e.preventDefault();
        // login = document.querySelector("#login");
        let json = {
            username: login.querySelector('input[name="user"]').value,
            password: login.querySelector('input[name="pass"]').value
        }

        let returned = await fetch("login", {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(json)
        });

        let data = await returned.json();
        if (returned.status === 200 && data.logged_in) {
            window.location.href = "/ers/"
        } else {
            if (returned.status === 401) {
                let alert = new Alert(AlertType.Failed, "Invalid username/password");
                alert.setHeader("Login failed");
                pushAlert(alert);
            } else {
                let alert = new Alert(Alert.Failed, "There was an error when logging you in");
                alert.setHeader("Error");
                pushAlert(alert);
            }
        }
    });
    
});