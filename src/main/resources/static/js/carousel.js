function carouselNext(button) {
    const id = button.dataset.carousel;
    scrollCarousel(id, 1);
}

function carouselPrev(button) {
    const id = button.dataset.carousel;
    scrollCarousel(id, -1);
}

function scrollCarousel(id, direction) {

    const carousel = document.getElementById(id);

    if (!carousel) {
        return;
    }

    const card = carousel.querySelector(".carousel-card");

    if (!card) {
        return;
    }

    const style = getComputedStyle(carousel);

    const gap = parseInt(style.gap || style.columnGap || 20);

    const cardsPerScroll = getCardsPerScroll();

    const distance =
        (card.offsetWidth + gap) * cardsPerScroll;

    carousel.scrollBy({
        left: direction * distance,
        behavior: "smooth"
    });

}

function getCardsPerScroll() {

    const width = window.innerWidth;

    if (width >= 1600) {
        return 5;
    }

    if (width >= 1400) {
        return 4;
    }

    if (width >= 1200) {
        return 3;
    }

    if (width >= 768) {
        return 2;
    }

    return 1;

}

function updateCarouselButtons(carousel) {

    const id = carousel.id;

    const prev = document.querySelector(
        `.carousel-btn.prev[data-carousel="${id}"]`
    );

    const next = document.querySelector(
        `.carousel-btn.next[data-carousel="${id}"]`
    );

    if (!prev || !next) {
        return;
    }

    const maxScroll =
        carousel.scrollWidth - carousel.clientWidth;

    prev.classList.toggle(
        "disabled",
        carousel.scrollLeft <= 5
    );

    next.classList.toggle(
        "disabled",
        carousel.scrollLeft >= maxScroll - 5
    );

}

function initCarousel(carousel) {

    updateCarouselButtons(carousel);

    carousel.addEventListener("scroll", () => {
        updateCarouselButtons(carousel);
    });

}

function initCarousels() {

    document
        .querySelectorAll(".category-carousel")
        .forEach(initCarousel);

}

document.addEventListener("DOMContentLoaded", initCarousels);

window.addEventListener("resize", () => {

    document
        .querySelectorAll(".category-carousel")
        .forEach(updateCarouselButtons);

});