import React, {Component} from 'react';
import './index.css'
import * as HttpService from "./HttpService";
import {Redirect} from 'react-router-dom';
import BackButton from "./BackButton";

class PersonalData extends Component {
  constructor(props) {
    super(props);
    this.state = {
      name: '',
      surname: '',
      phoneNumber: '',
      email: '',
      reservationId: '',
    }
  }

  //TODO to mutowanie statusu blee, mozesz najpierw sprawdzic status i jesli jest inny niz 200 zrobic alert
  //HttpService.postJson('cinemaHall/addPerson', person)
  //       .then(results => {
  //         if (results.status === 200) {
  //           return results.text()
  //         } else {
  //           alert("Invalid data form")
  //         }.then(reservationId => {...
  addPerson = (event) => {
    const person = {
      name: this.state.name,
      surname: this.state.surname,
      phoneNumber: this.state.phoneNumber,
      email: this.state.email
    };
    var status = 200;
    HttpService.postJson('cinemaHall/addPerson', person)
      .then(results => {
        status = results.status;
        return results.text();
      }).then(reservationId => {
      if (status === 200) {
        this.setState({reservationId: reservationId});
        this.handleOnClick()
      } else {
        alert("Invalid data form");
      }
    });
    event.preventDefault()
  };

  handleOnClick = () => {
    this.setState({redirect: true});
  };

  render() {
    return (
      <div className={"personalData"}>
        <form onSubmit={this.addPerson}>
          <div className={"PersonalDataForm"}>
            <label htmlFor="name">Name</label>
            <input
              type="text"
              value={this.state.name}
              onChange={(event) => {
                this.setState({name: event.target.value});
              }}
              pattern={"\\p{L}+"}
              placeholder={"John"}
              required
            />
            <label htmlFor="surname">Surname</label>
            <input
              type="text"
              value={this.state.surname}
              onChange={(event) => {
                this.setState({surname: event.target.value});
              }}
              pattern={"\\p{L}+"}
              placeholder={"Doe"}
              required
            />
            <label htmlFor="email">Email</label>
            <input
              type="email"
              value={this.state.email}
              onChange={(event) => {
                this.setState({email: event.target.value});
              }}
              pattern={new RegExp("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$").ignoreCase}
              placeholder={"johndoe@gmail.com"}
              required
            />
            <label htmlFor="name">Telephone</label>
            <input
              type="tel"
              value={this.state.phoneNumber}
              onChange={(event) => {
                this.setState({phoneNumber: event.target.value});
              }}
              pattern="\d{9}|(?:\d{3}-){2}\d{3}"
              placeholder={"123-456-789 or 123456789"}
              required
            />
          </div>
          {this.state.redirect ? <Redirect push
                                           to={`/reservationSummary`}/> : null}
          <button className={"summaryButton"} type="submit">summary</button>
          <BackButton/>
        </form>
      </div>
    )
  }

}

export default PersonalData;