async function getRecommendations() {
    const genre = document.getElementById("genre").value;
    const rating = document.getElementById("rating").value;
    const year = document.getElementById("year").value;
    const results = document.getElementById("results");
    const resultTitle = document.getElementById("resultTitle");
    const resultCount = document.getElementById("resultCount");
    results.innerHTML = "<div class='empty-state'><strong>Finding your movies...</strong><span>Just a moment.</span></div>";
    try {
        const url = `/api/movies/recommend?genre=${encodeURIComponent(genre)}&rating=${rating}&year=${year}`;
        const response = await fetch(url);
        if (!response.ok) {
            throw new Error("Unable to get recommendations");
        }
        const movies = await response.json();
        displayMovies(movies);
    } catch (error) {
        resultTitle.textContent = "Something went wrong";
        resultCount.textContent = "";
        results.innerHTML = "<div class='empty-state'><strong>Couldn't load the movies.</strong><span>Check that the Spring Boot application is running.</span></div>";
        console.error(error);
    }
}

function displayMovies(movies) {
    const results = document.getElementById("results");
    const resultTitle = document.getElementById("resultTitle");
    const resultCount = document.getElementById("resultCount");
    results.innerHTML = "";

    if (movies.length === 0) {
        resultTitle.textContent = "No matches found";
        resultCount.textContent = "";
        results.innerHTML = "<div class='empty-state'><strong>Nothing matched your preferences.</strong><span>Try lowering the rating or choosing a different year.</span></div>";
        return;
    }

    resultTitle.textContent = "Recommended for you";
    resultCount.textContent = `${movies.length} movie${movies.length === 1 ? "" : "s"} found`;

    movies.forEach(movie => {
        results.innerHTML += `
            <div class="movie"'>
                <img src="${movie.poster}" alt="${movie.title}">
                <div class="movie-info">
                    <h3>${movie.title}</h3>
                    <p class="movie-meta">${movie.genre} · ${movie.year}</p>
                    <p class="rating">★ ${movie.rating.toFixed(1)}</p>
                </div>
            </div>`;
    });
}
