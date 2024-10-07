<template>
    <div>
        <Number label="ProductId" v-model="value.productId" :editMode="editMode"/>
        <String label="ProductName" v-model="value.productName" :editMode="editMode"/>
        <Number label="Qty" v-model="value.qty" :editMode="editMode"/>

        <v-divider class="border-opacity-100 my-divider"></v-divider>
        <v-layout row justify-end>
            <v-btn
                width="64px"
                color="primary"
                @click="save"
            >
                저장
            </v-btn>
        </v-layout>
    </div>
</template>


<script>

import BaseEntity from './base-ui/BaseEntity.vue'
import Number from './primitives/Number.vue'
import String from './primitives/String.vue'
import Number from './primitives/Number.vue'

export default {
    name: 'Order',
    mixins:[BaseEntity],
    components:{
        Number,
        String,
        Number,
    },
    
    data: () => ({
        path: "orders"
    }),
    created(){
    },
    computed:{
    },
    methods: {
    },
}
</script>
