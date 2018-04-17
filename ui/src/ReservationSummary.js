import React, {Component} from 'react';
import * as HttpService from "./HttpService";
import {Redirect} from 'react-router-dom';
import BackButton from "./BackButton";


class ReservationSummary extends Component {

    constructor() {
        super();
        this.state = {
            personalData: [],
            ticketData: null,
            orderResponse: [],
        };
    }

    componentDidMount() {
        this.getSummary()
    }

    getSummary = () => {
        HttpService.fetchJson("reservationSummary")
            .then(data => {
                console.log("Success - summary: ", data);
                this.setState({personalData: data.personalData, ticketData: data.ticketData})
            })
    };

    handleClick = () => {
        return HttpService.post(`/payment`)
            .then(results => {
                return results.json();
            }).then(data => {
                window.location = data.redirectUri
            });

    };

    render() {
        console.log("this.state.ticketData", this.state.ticketData)
        return this.state.ticketData ? (
            <div className={"summary"}>
                <div className={"summaryData"}>
                    <h2>Summary</h2>
                    <li>
                        <text className={"reservationData"}>Name:</text>
                        {this.state.personalData.name}</li>
                    <li>
                        <text className={"reservationData"}>Surname:</text>
                        {this.state.personalData.surname}</li>
                    <li>
                        <text className={"reservationData"}>Email:</text>
                        {this.state.personalData.email}</li>
                    <li>
                        <text className={"reservationData"}>Phone:</text>
                        {this.state.personalData.phoneNumber}</li>
                    <li>
                        <text className={"reservationData"}>Movie:</text>
                        "{this.state.ticketData.movieTitle}"
                    </li>
                    <li>
                        <text className={"reservationData"}>Date of projection:</text>
                        {this.state.ticketData.projectionDate}</li>
                    <li>
                        <text className={"reservationData"}>Hour of projection:</text>
                        {this.state.ticketData.projectionHour}</li>
                    <li>
                        <text className={"reservationData"}>Seat Nr: &emsp; Row: &emsp;&emsp;Type:&emsp;&emsp;Price:</text>
                        {this.state.ticketData.seatAndPriceDetails.map((seatAndPrice, idx) => {
                            return (
                                <li className={"reservationSeat"}>&emsp;{seatAndPrice.seat.seatNumber} &emsp;&emsp;&emsp;&emsp;&emsp; {seatAndPrice.seat.rowNumber}&emsp;&emsp;&emsp;&emsp;
                                  {seatAndPrice.ticketPrice.ticketType}&emsp;&emsp;&emsp;  {seatAndPrice.ticketPrice.ticketValue}</li>)
                            }
                        )}</li>
                </div>

                {this.state.redirect ? <Redirect push to={`/payment/${this.props.match.params.reservationId}`}/> : null}
                <div className={"buttons"}>

                    <BackButton/>
                    <button className={"payButton"} type="button" onClick={this.handleClick}>Pay</button>
                </div>

            </div>
        ) : null;
    }


}

export default ReservationSummary;