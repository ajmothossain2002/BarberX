import { IsString, IsInt, Min, IsNumber, MinLength } from 'class-validator';

export class CreateServiceDto {
    @IsString()
    @MinLength(2)
    name: string;

    @IsInt()
    @Min(5)
    duration: number; // minutes

    @IsNumber()
    @Min(0)
    price: number;
}
