let modalId = "";
let modal = false;
document.addEventListener("DOMContentLoaded", async function () {
    document.querySelector(".modal").addEventListener('click', function (e) {
        e.stopPropagation();
    });

    document.body.addEventListener('click', function () {
        if (modal) {
            toggleModal(modalId);
        }
    });
});

function toggleModal(id) {
    if (modal && modalId != id) {
        toggleModal(modalId);
    }
    modalId = id;
    document.querySelector('#modal-wrapper').classList.toggle('hidden');
    document.body.classList.toggle('body-overflow');
    document.querySelector("#" + id).classList.toggle('hidden');
    modal = !modal;
}