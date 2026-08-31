document.addEventListener("DOMContentLoaded", function () {

    const imageInput = document.getElementById("imageFile");
    const cropImage = document.getElementById("cropImage");
    const cropModalElement = document.getElementById("cropModal");
    const cropConfirmButton = document.getElementById("cropConfirmButton");
    const zoomInButton = document.getElementById("zoomInButton");
    const zoomOutButton = document.getElementById("zoomOutButton");

    if (!imageInput ||
        !cropImage ||
        !cropModalElement ||
        !cropConfirmButton) {
        return;
    }

    let cropper = null;
    let cropConfirmed = false;

    const cropModal =
        bootstrap.Modal.getOrCreateInstance(cropModalElement);

    /*
     * SELECTARE IMAGINE
     */
    imageInput.addEventListener("change", function (event) {

        const file = event.target.files[0];

        if (!file) {
            return;
        }

        if (!file.type.startsWith("image/")) {
            imageInput.value = "";
            return;
        }

        const imageUrl = URL.createObjectURL(file);

        cropImage.src = imageUrl;

        cropModal.show();
    });


    /*
     * INITIALIZARE CROPPER
     */
    cropModalElement.addEventListener(
        "shown.bs.modal",
        function () {

            if (cropper) {
                cropper.destroy();
                cropper = null;
            }

            cropImage.onload = function () {

                cropper = new Cropper(cropImage, {

                    aspectRatio: 4 / 3,

                    viewMode: 1,

                    dragMode: "move",

                    autoCropArea: 0.9,

                    responsive: true,
                    restore: false,

                    guides: true,
                    center: true,
                    highlight: false,

                    cropBoxMovable: false,
                    cropBoxResizable: false,

                    toggleDragModeOnDblclick: false,

                    zoomable: true,
                    zoomOnWheel: true,
                    zoomOnTouch: true,

                    movable: true,
                    scalable: true,

                    rotatable: false
                });
            };

            /*
             * Imaginea poate fi deja încărcată.
             * În acest caz declanșăm manual onload.
             */
            if (cropImage.complete) {
                cropImage.onload();
            }

        }
    );


    /*
     * ZOOM +
     */
    if (zoomInButton) {

        zoomInButton.addEventListener(
            "click",
            function () {

                if (cropper) {
                    cropper.zoom(0.1);
                }

            }
        );
    }


    /*
     * ZOOM -
     */
    if (zoomOutButton) {

        zoomOutButton.addEventListener(
            "click",
            function () {

                if (cropper) {
                    cropper.zoom(-0.1);
                }

            }
        );
    }


    /*
     * CONFIRMARE CROP
     */
    cropConfirmButton.addEventListener(
        "click",
        function () {

            if (!cropper) {
                return;
            }

            const canvas =
                cropper.getCroppedCanvas({

                    width: 1200,
                    height: 900,

                    imageSmoothingEnabled: true,
                    imageSmoothingQuality: "high"
                });


            canvas.toBlob(
                function (blob) {

                    if (!blob) {
                        return;
                    }

                    const croppedFile =
                        new File(
                            [blob],
                            "cover.jpg",
                            {
                                type: "image/jpeg",
                                lastModified: Date.now()
                            }
                        );


                    const dataTransfer =
                        new DataTransfer();

                    dataTransfer.items.add(
                        croppedFile
                    );

                    imageInput.files =
                        dataTransfer.files;


                    /*
                     * PREVIEW
                     */
                    const previewContainer =
                        document.getElementById(
                            "imagePreviewContainer"
                        );

                    const preview =
                        document.getElementById(
                            "imagePreview"
                        );

                    if (preview &&
                        previewContainer) {

                        preview.src =
                            URL.createObjectURL(blob);

                        previewContainer.classList
                            .remove("d-none");
                    }


                    cropConfirmed = true;

                    cropModal.hide();

                },
                "image/jpeg",
                0.90
            );
        }
    );


    /*
     * MODAL ÎNCHIS
     */
    cropModalElement.addEventListener(
        "hidden.bs.modal",
        function () {

            if (cropper) {

                cropper.destroy();

                cropper = null;
            }

            cropImage.onload = null;
            cropImage.src = "";

            cropConfirmed = false;
        }
    );

});