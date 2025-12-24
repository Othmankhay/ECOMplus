import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {NgFor, NgIf} from '@angular/common';

@Component({
  selector: 'app-products',
  standalone: true,
  imports: [NgIf, NgFor],
  templateUrl: './products.html',
  styleUrl: './products.css',
})
export class Products implements OnInit {

  products: any;

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.http.get("http://localhost:8888/api/products").subscribe(
      {
        next : (data)=>{
          this.products=data;
        },
        error : (err)=>{}
      }
    );
  }

}
