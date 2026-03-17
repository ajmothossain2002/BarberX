import {
    Controller,
    Get,
    Post,
    Body,
    Param,
    Patch,
    Delete,
    Query,
    UseGuards,
    Request,
} from '@nestjs/common';
import { JwtAuthGuard } from '../auth/jwt-auth.guard';
import { ShopsService } from './shops.service';
import { CreateShopDto, UpdateShopDto } from './dto';

@Controller('shops')
export class ShopsController {
    constructor(private shopsService: ShopsService) { }

    @Post()
    @UseGuards(JwtAuthGuard)
    create(@Request() req, @Body() dto: CreateShopDto) {
        return this.shopsService.create(req.user.userId, dto);
    }

    @Get()
    findAll(@Query('city') city?: string) {
        return this.shopsService.findAll(city);
    }

    @Get(':id')
    findOne(@Param('id') id: string) {
        return this.shopsService.findOne(id);
    }

    @Patch(':id')
    @UseGuards(JwtAuthGuard)
    update(@Request() req, @Param('id') id: string, @Body() dto: UpdateShopDto) {
        return this.shopsService.update(req.user.userId, id, dto);
    }

    @Delete(':id')
    @UseGuards(JwtAuthGuard)
    delete(@Request() req, @Param('id') id: string) {
        return this.shopsService.delete(req.user.userId, id);
    }
}
