import React, {Component} from 'react';
import * as HttpService from "./HttpService";

class MovieDetails extends Component {
  constructor() {
    super();
    this.days = ['SUNDAY', 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY'];
    this.state = {
      chosenMovieId: [],
    };
  }

  today = () => {
    const date = new Date();
    return this.days[date.getDay()];
  }

  componentDidMount() {
    HttpService.fetchJson(`whatsOn/${this.props.match.params.chosenMovieId}`)
      .then(data => {
        this.setState({whatsOnByMovies: data})
      })
  };

  render() {
    console.log(this.state.whatsOnByMovies)
    return (
      this.state.whatsOnByMovies ?
        <div className={"movieDetails"}>
          <div className={"movieDescImg"}><img src={decodeURIComponent(this.state.whatsOnByMovies[0].movieImageUrl)} alt=""/></div>
          <div className={"movieDesc"}>
            <h1>"{this.state.whatsOnByMovies[0].movieTitle}"</h1>
            <p> Duration: {this.state.whatsOnByMovies[0].movieDurationInMinutes} minutes </p>
            <p> Description: {this.state.whatsOnByMovies[0].movieDescription}</p>
            <p>Today's projection: </p>
          </div>
          <div className={"projectionHours"}>
            {this.state.whatsOnByMovies.map((movie, idx) =>
              movie.dayOfProjection === this.today() ?
                <li key={idx}>{movie.hourOfProjection}</li>
                : null
            )}
          </div>
        </div> : null

    );
  }
}


export default MovieDetails;