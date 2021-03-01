

import React from 'react'
import { Redirect } from 'react-router-dom'
import { isLoggedIn } from '../auth'

interface Props {
}

export default function Home(props:Props) {

    if(!isLoggedIn()) {
        return <Redirect to='/login'/>
    }

    return <div>Home goes here</div>
}

