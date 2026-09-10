document.addEventListener("DOMContentLoaded", function () {

    function openVideoFullscreen(trigger) {

        const video = trigger.matches("video")
            ? trigger
            : trigger.querySelector("video");

        if (!video) {
            return;
        }

        video.controls = true;
        video.muted = false;

        if (typeof video.webkitEnterFullscreen === "function") {
            video.webkitEnterFullscreen();
        } else {
            const requestFullscreen =
                video.requestFullscreen ||
                video.webkitRequestFullscreen ||
                video.msRequestFullscreen;

            if (requestFullscreen) {
                const request = requestFullscreen.call(video);

                if (request && typeof request.catch === "function") {
                    request.catch(function () {
                        // Redarea normală rămâne disponibilă dacă browserul blochează fullscreen.
                    });
                }
            }
        }

        video.play().catch(function () {
            // Browserul poate aștepta o interacțiune suplimentară pentru redare.
        });
    }

    document.querySelectorAll("[data-fullscreen-video]").forEach(function (trigger) {
        trigger.addEventListener("click", function () {
            openVideoFullscreen(trigger);
        });

        trigger.addEventListener("keydown", function (event) {
            if (event.key === "Enter" || event.key === " ") {
                event.preventDefault();
                openVideoFullscreen(trigger);
            }
        });
    });
});
